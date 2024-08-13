package org.carlspring.strongbox.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.carlspring.strongbox.domain.UserEntry;
import org.carlspring.strongbox.users.dto.User;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.EncodedPasswordUser;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService.OrientDb;
import org.carlspring.strongbox.users.service.impl.YamlUserService.Yaml;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;
import org.carlspring.strongbox.users.userdetails.StrongboxExternalUsersCacheManager;
import org.carlspring.strongbox.users.userdetails.StrongboxUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles({ "test", "ConfigurableProviderManagerTestConfig" })
@TestPropertySource(properties = { "strongbox.config.file.authentication.providers=classpath:eudst-strongbox-authentication-providers.xml",
                                   "strongbox.authentication.providers.yaml=classpath:/etc/conf/eudst-strongbox-authentication-providers.yaml",
                                   "users.external.cache.seconds=3600",
                                   "strongbox.users.config.yaml=classpath:/etc/conf/eudst-strongbox-security-users.yaml" })
@ContextConfiguration(classes = TestConfig.class)
public class ConfigurableProviderManagerTest
{

    private static final String TEST_USER = "test-user";

    private static final String TEST_DUPLICATE_USER = "test-duplicate";

    private static final String TEST_CONCURRENT_USER = "test-concurrent";

    @Value("${users.external.cache.seconds}")
    private int externalUsersInvalidateSeconds;

    @Inject
    @OrientDb
    private UserService orientDbUserService;

    @Inject
    @Yaml
    private UserService yamlUserService;

    @Inject
    private StrongboxExternalUsersCacheManager strongboxUserManager;

    @Inject
    private UserDetailsService userDetailsService;

    @Inject
    private PasswordEncoder passwordEncoder;

    @Test
    public void testApplicationContext() 
    {
        // Check that we have proper implementation of UserDetailsService injected by Spring
        assertThat(userDetailsService).isInstanceOf(ConfigurableProviderManager.class);
    }
    
    @Test
    public void testExternalUserShouldBeReplacedWhenExpired()
    {
        // Given: сached external user
        UserDto user = new UserDto();
        user.setUsername(TEST_DUPLICATE_USER);
        user.setPassword("foobarpasswrod");
        user.setSourceId("someExternalUserSourceId");
        user.setLastUpdate(new Date());
        strongboxUserManager.cacheExternalUserDetails("someExternalUserSourceId", new StrongboxUserDetails(user));
        
        // Check that external user cached
        UserDetails userDetails = userDetailsService.loadUserByUsername(TEST_DUPLICATE_USER);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(SpringSecurityUser.class);
        assertThat(((SpringSecurityUser)userDetails).getSourceId()).isEqualTo("someExternalUserSourceId");
        
        // Make this user expired
        expireUser(TEST_DUPLICATE_USER);

        // Expired user should be replaced with user from yaml
        userDetails = userDetailsService.loadUserByUsername(TEST_DUPLICATE_USER);
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(TEST_DUPLICATE_USER);
        assertThat(userDetails).isInstanceOf(SpringSecurityUser.class);
        assertThat(((SpringSecurityUser)userDetails).getSourceId()).isEqualTo("yamlUserDetailService");
    }

    @Test
    public void testExternalUserShouldBeUpdatedWhenExpired()
    {
        // Check that there is no external user
        assertThat(strongboxUserManager.findByUsername(TEST_USER)).isNull();

        // Load and cache external user
        assertThat(userDetailsService.loadUserByUsername(TEST_USER)).isNotNull();

        // Check that external user cached
        User externalUser = strongboxUserManager.findByUsername(TEST_USER);
        String externalUserId = ((UserEntry)externalUser).getObjectId();
        assertThat(externalUser).isNotNull();
        assertThat(externalUserId).isNotNull();

        // Check that external user can't be modyfied
        User externalUserToSave = externalUser;
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> orientDbUserService.save(externalUserToSave))
                                                              .withMessageMatching("Can't modify external users.");

        // Update external password
        String newPassword = UUID.randomUUID().toString();
        UserDto userToUpdate = new UserDto();
        userToUpdate.setUsername(TEST_USER);
        userToUpdate.setPassword(newPassword);
        yamlUserService.updateAccountDetailsByUsername(new EncodedPasswordUser(userToUpdate, passwordEncoder));

        // Invalidate user cache
        expireUser(TEST_USER);

        // Update user cahce
        assertThat(userDetailsService.loadUserByUsername(TEST_USER)).isNotNull();

        // Cached user password should be updated
        externalUser = strongboxUserManager.findByUsername(TEST_USER);
        assertThat(externalUser).isNotNull();
        assertThat(passwordEncoder.matches(newPassword, externalUser.getPassword())).isTrue();
        // ID shouldn't be changed
        assertThat(((UserEntry)externalUser).getObjectId()).isEqualTo(externalUserId);
    }

    @Test
    public void testConcurrentExternalUserCache()
    {
        // new user
        assertThat(concurrentLoadUsers(TEST_CONCURRENT_USER)).allMatch(u -> TEST_CONCURRENT_USER.equals(u.getUsername()));
        
        // expired user update
        expireUser(TEST_CONCURRENT_USER);
        assertThat(concurrentLoadUsers(TEST_CONCURRENT_USER)).allMatch(u -> TEST_CONCURRENT_USER.equals(u.getUsername()));
        
        // expired user replace
        ((OrientDbUserService) orientDbUserService).expireUser(TEST_CONCURRENT_USER, true);
        assertThat(concurrentLoadUsers(TEST_CONCURRENT_USER)).allMatch(u -> "yamlUserDetailService".equals(u.getSourceId()));
    }

    private void expireUser(String username)
    {
        ((OrientDbUserService) orientDbUserService).expireUser(username, false);
    }

    private List<SpringSecurityUser> concurrentLoadUsers(String testConcurrentUser)
    {
        return IntStream.range(0, 4)
                        .mapToObj(i -> userDetailsService.loadUserByUsername(TEST_CONCURRENT_USER))
                        .map(u -> (SpringSecurityUser) u)
                        .parallel()
                        .collect(Collectors.toList());
    }

    @Profile("ConfigurableProviderManagerTestConfig")
    @Import(HazelcastConfiguration.class)
    @Configuration
    public static class ConfigurableProviderManagerTestConfig
    {

        @Primary
        @Bean
        public HazelcastInstanceId hazelcastInstanceIdAcctit()
        {
            return new HazelcastInstanceId("ExternalUserDetailsServiceTest-hazelcast-instance");
        }

    }
}

package org.carlspring.strongbox.config;

import org.carlspring.strongbox.authentication.AuthenticationConfig;
import org.carlspring.strongbox.security.CustomAccessDeniedHandler;
import org.carlspring.strongbox.security.authentication.Http401AuthenticationEntryPoint;
import org.carlspring.strongbox.security.authentication.StrongboxAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.suppliers.AuthenticationSupplier;
import org.carlspring.strongbox.security.authentication.suppliers.AuthenticationSuppliers;
import org.carlspring.strongbox.security.vote.MethodAccessDecisionManager;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.users.domain.SystemRole;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;

import javax.inject.Inject;
import javax.inject.Qualifier;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@ComponentScan({ "org.carlspring.strongbox.security" })
@Import({ DataServiceConfig.class,
          UsersConfig.class,
          AuthenticationConfig.class})
@Configuration
@EnableWebSecurity
public class WebSecurityConfig
        extends WebSecurityConfigurerAdapter
{

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    @Inject
    private AuthenticationManager authenticationManager;

    @Inject
    private List<AuthenticationSupplier> suppliers;


    @Override
    public void init(WebSecurity web)
            throws Exception
    {
        super.init(web);
        DefaultHttpFirewall httpFirewall = new DefaultHttpFirewall();
        httpFirewall.setAllowUrlEncodedSlash(true);
        web.httpFirewall(httpFirewall);
    }

    @Override
    protected void configure(HttpSecurity http)
            throws Exception
    {
        http.addFilterAfter(strongboxAuthenticationFilter(),
                            ExceptionTranslationFilter.class)
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
            .accessDeniedHandler(accessDeniedHandler())
            // TODO SB-813
            .authenticationEntryPoint(customBasicAuthenticationEntryPoint())
            .and()
            // this part of code is necessary to secure endpoints for not authorized users
            .authorizeRequests()
            .requestMatchers(EndpointRequest.toAnyEndpoint())
            .hasAuthority("ADMIN")
            .and()
            .anonymous()
            .authenticationFilter(anonymousAuthenticationFilter())
            .and()
            .cors()
            .and()
            .csrf()
            .disable();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(ConfigurationManagementService configurationManagementService)
    {
        final CorsConfiguration configuration = new CorsConfiguration();
        final org.carlspring.strongbox.configuration.CorsConfiguration internalCorsConfiguration = configurationManagementService
                                                                                                           .getConfiguration()
                                                                                                           .getCorsConfiguration();
        if (internalCorsConfiguration != null)
        {
            if (internalCorsConfiguration.getAllowedMethods() != null)
            {
                configuration.setAllowedMethods(new ArrayList<>(internalCorsConfiguration.getAllowedMethods()));
            }
            if (internalCorsConfiguration.getAllowedHeaders() != null)
            {
                configuration.setAllowedHeaders(new ArrayList<>(internalCorsConfiguration.getAllowedHeaders()));
            }
            if (internalCorsConfiguration.getAllowedOrigins() != null)
            {
                configuration.setAllowedOrigins(new ArrayList<>(internalCorsConfiguration.getAllowedOrigins()));
            }
            if (internalCorsConfiguration.getExposedHeaders() != null)
            {
                configuration.setExposedHeaders(new ArrayList<>(internalCorsConfiguration.getExposedHeaders()));
            }
            if (internalCorsConfiguration.getAllowCredentials() != null)
            {
                configuration.setAllowCredentials(BooleanUtils.isTrue(internalCorsConfiguration.getAllowCredentials()));
            }
            if (internalCorsConfiguration.getMaxAge() != null)
            {
                configuration.setMaxAge(internalCorsConfiguration.getMaxAge());
            }
        }

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler()
    {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    @UnauthorizedEntyPoint
    AuthenticationEntryPoint customBasicAuthenticationEntryPoint()
    {
        return new Http401AuthenticationEntryPoint();
    }

    @Bean
    StrongboxAuthenticationFilter strongboxAuthenticationFilter()
    {
        return new StrongboxAuthenticationFilter(new AuthenticationSuppliers(suppliers), authenticationManager);
    }

    @Bean
    AnonymousAuthenticationFilter anonymousAuthenticationFilter()
    {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS");
        authorities.addAll(authoritiesProvider.getRuntimeRole(SystemRole.ANONYMOUS.name()).getAccessModel().getApiAuthorities());

        return new AnonymousAuthenticationFilter("strongbox-unique-key",
                                                 "anonymousUser",
                                                 authorities);
    }


    /**
     * This Configuration enables @PreAuthorize annotations
     *
     * @author Sergey Bespalov
     */
    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class MethodSecurityConfig
            extends GlobalMethodSecurityConfiguration
    {

        @Inject
        MethodAccessDecisionManager methodAccessDecisionManager;

        @Override
        protected AccessDecisionManager accessDecisionManager()
        {
            return methodAccessDecisionManager;
        }

    }

    @Configuration
    public static class SharedObjectsConfig{

        @Bean
        AuthenticationTrustResolver authenticationTrustResolver() {
            return new AuthenticationTrustResolverImpl();
        }
        
    }

    @Qualifier
    public static @interface UnauthorizedEntyPoint
    {

    }
}

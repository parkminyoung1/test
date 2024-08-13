package org.carlspring.strongbox.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.config.PropertiesPathResolverConfig.PropertiesPathResolver;
import static org.carlspring.strongbox.config.PropertiesPathResolverConfig.PropertiesPathResolver.PREFIX_OVERRIDE_PROPERTY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesPathResolverConfigTest
{
    private static final String CURRENT_DIR = System.getProperty("user.dir");
    private static final String CUSTOM_PATH_PROP = "strongbox.scary-t.file";
    private static final String STRONGBOX_HOME_PROP = "strongbox.home";
    private static final String STRONGBOX_HOME_DEFAULT = "/Users/keyboard-kitty/work/sbox";
    private static final String DEFAULT_PATH = "etc/configzzz.yaml";

    private Environment env;
    private PropertiesPathResolver resolver;

    @BeforeEach
    public void setUp()
    {
        env = mock(Environment.class);
        resolver = new PropertiesPathResolver(env);
    }

    @Test
    public void testCustomAbsolutePath()
    {
        final String customPropValue = "/Users/sleepy/one/conf.yaml";
        prepareProps(STRONGBOX_HOME_DEFAULT, customPropValue, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo("file://" + customPropValue);
    }

    @Test
    public void testCustomAbsolutePathAlreadyPrefixedWithClasspathScheme()
    {
        final String customPropValue = "classpath:conf.yaml";
        prepareProps(STRONGBOX_HOME_DEFAULT, customPropValue, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo(customPropValue);
    }

    @Test
    public void testCustomAbsolutePathAlreadyPrefixedWithFileScheme()
    {
        final String customPropValue = "file:conf.yaml";
        prepareProps(STRONGBOX_HOME_DEFAULT, customPropValue, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo(customPropValue);
    }

    @Test
    public void testCustomRelativePathToCurrentDir()
    {
        final String customPropValue = "./conf.yaml";
        prepareProps(STRONGBOX_HOME_DEFAULT, customPropValue, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo("file://" + CURRENT_DIR + "/conf.yaml");
    }

    @Test
    public void testCustomRelativePathToParentDir()
    {
        final String customPropValue = "../conf.yaml";
        prepareProps(STRONGBOX_HOME_DEFAULT, customPropValue, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo("file://" + CURRENT_DIR + "/../conf.yaml");
    }

    @Test
    public void testDefaultPathWithOverridePrefix()
    {
        final String defaultPrefixOverride = "classpath:";
        prepareProps(STRONGBOX_HOME_DEFAULT, null, defaultPrefixOverride);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo(defaultPrefixOverride + DEFAULT_PATH);
    }

    @Test
    public void testDefaultPathWithAbsoluteStrongboxHomeNoEndingSlash()
    {
        prepareProps(STRONGBOX_HOME_DEFAULT, null, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo("file://" + STRONGBOX_HOME_DEFAULT + "/" + DEFAULT_PATH);
    }

    @Test
    public void testDefaultPathWithAbsoluteStrongboxHomeWithEndingSlash()
    {
        prepareProps(STRONGBOX_HOME_DEFAULT + "/", null, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo("file://" + STRONGBOX_HOME_DEFAULT + "/" + DEFAULT_PATH);
    }

    @Test
    public void testDefaultPathWithRelativeCurrentDirStrongboxHome()
    {
        prepareProps(".", null, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo("file://" + CURRENT_DIR + "/" + DEFAULT_PATH);
    }

    @Test
    public void testDefaultPathWithRelativeParentDirStrongboxHome()
    {
        prepareProps("..", null, null);
        assertThat(resolver.resolve(CUSTOM_PATH_PROP, DEFAULT_PATH)).isEqualTo("file://" + CURRENT_DIR + "/../" + DEFAULT_PATH);
    }

    private void prepareProps(final String strongboxHome, final String customPropValue, final String defaultPrefixOverride)
    {
        when(env.getRequiredProperty(STRONGBOX_HOME_PROP)).thenReturn(strongboxHome);
        when(env.getProperty(CUSTOM_PATH_PROP)).thenReturn(customPropValue);
        when(env.getProperty(PREFIX_OVERRIDE_PROPERTY)).thenReturn(defaultPrefixOverride);
    }
}

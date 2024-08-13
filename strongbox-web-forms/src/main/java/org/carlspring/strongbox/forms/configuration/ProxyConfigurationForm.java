package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;

/**
 * @author Pablo Tirado
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProxyConfigurationForm
{

    @NotBlank(message = "A host must be specified.", groups = ProxyConfigurationFormChecks.class)
    private String host;

    @NotNull(message = "A port must be provided.", groups = ProxyConfigurationFormChecks.class)
    @Min(value = 1, message = "The port number must be an integer between 1 and 65535.", groups = ProxyConfigurationFormChecks.class)
    @Max(value = 65535, message = "The port number must be an integer between 1 and 65535.", groups = ProxyConfigurationFormChecks.class)
    private Integer port;

    @NotBlank(message = "A proxy type must be specified.", groups = ProxyConfigurationFormChecks.class)
    @Pattern(regexp = "DIRECT|HTTP|SOCKS4|SOCKS5",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "The proxy type must contain one the following strings as value: DIRECT, HTTP, SOCKS4, SOCKS5",
            groups = ProxyConfigurationFormChecks.class)
    private String type;

    private String username;

    private String password;

    private List<String> nonProxyHosts = Lists.newArrayList();

    public ProxyConfigurationForm()
    {
    }

    public ProxyConfigurationForm(String host,
                                  Integer port,
                                  String type,
                                  String username,
                                  String password,
                                  List<String> nonProxyHosts)
    {
        this.host = host;
        this.port = port;
        this.type = type;
        this.username = username;
        this.password = password;
        this.nonProxyHosts = nonProxyHosts;
    }

    @JsonIgnore()
    public static ProxyConfigurationForm fromConfiguration(ProxyConfiguration source)
    {
        ProxyConfiguration configuration = Optional.ofNullable(source).orElse(
                new ProxyConfiguration(new MutableProxyConfiguration())
        );

        return new ProxyConfigurationForm(configuration.getHost(),
                                          configuration.getPort(),
                                          configuration.getType(),
                                          configuration.getUsername(),
                                          null,
                                          configuration.getNonProxyHosts());
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public List<String> getNonProxyHosts()
    {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(List<String> nonProxyHosts)
    {
        this.nonProxyHosts = nonProxyHosts;
    }

    @JsonIgnore()
    public MutableProxyConfiguration getMutableProxyConfiguration()
    {
        return new MutableProxyConfiguration(this.host, this.port, this.username, this.password, this.type,
                                             this.nonProxyHosts);
    }

    public interface ProxyConfigurationFormChecks
            extends Serializable
    {
        // validation group marker interface for fields.
    }

}

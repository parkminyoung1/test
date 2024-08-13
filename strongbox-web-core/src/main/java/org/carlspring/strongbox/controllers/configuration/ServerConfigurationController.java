package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.InstanceNameEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.forms.configuration.CorsConfigurationForm;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm;
import org.carlspring.strongbox.forms.configuration.ServerSettingsForm;
import org.carlspring.strongbox.forms.configuration.SmtpConfigurationForm;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.support.ConfigurationException;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import io.swagger.annotations.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/configuration/strongbox")
@Api(value = "/api/configuration/strongbox")
public class ServerConfigurationController
        extends BaseConfigurationController
{

    static final String SUCCESSFUL_SAVE_SERVER_SETTINGS = "The server settings were updated successfully.";

    static final String FAILED_SAVE_SERVER_SETTINGS = "Server settings cannot be saved because the submitted form contains errors!";

    static final String FAILED_EMPTY_FORM = "Empty form was provided";

    private final Validator validator;

    public ServerConfigurationController(ConfigurationManagementService configurationManagementService,
                                         @Qualifier("localValidatorFactoryBean") Validator validator)
    {
        super(configurationManagementService);
        this.validator = validator;
    }

    @ApiOperation(value = "Updates the instance name.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The instance's name was updated."),
                            @ApiResponse(code = 400, message = "Could not update the instance's name.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_SET_INSTANCE_NAME', 'GLOBAL_CONFIGURATION_MANAGE')")
    @PutMapping(value = "/instanceName",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setInstanceName(@ApiParam(value = "The instance's name", required = true)
                                          @RequestBody InstanceNameEntityBody instanceNameEntityBody,
                                          @RequestHeader(HttpHeaders.ACCEPT) String accept) throws IOException
    {
        try
        {
            configurationManagementService.setInstanceName(instanceNameEntityBody.getInstanceName());

            logger.info("Set instance's name to [{}].", instanceNameEntityBody.getInstanceName());

            return ResponseEntity.ok(getResponseEntityBody("The instance's name was updated.", accept));
        }
        catch (ConfigurationException e)
        {
            String message = "Could not update the instance's name.";

            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Returns the instance name of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = String.class),
                            @ApiResponse(code = 404, message = "No value for instanceName has been defined yet.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_VIEW_INSTANCE_NAME', 'GLOBAL_CONFIGURATION_MANAGE')")
    @GetMapping(value = "/instanceName",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getInstanceName(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        String instanceName = configurationManagementService.getConfiguration().getInstanceName();
        if (instanceName != null)
        {
            return ResponseEntity.ok(getInstanceNameEntityBody(instanceName,
                                                               accept));
        }
        else
        {
            String message = "No value for instanceName has been defined yet.";

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Updates the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The base URL was updated."),
                            @ApiResponse(code = 400, message = "Could not update the base URL of the service.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_SET_BASE_URL', 'GLOBAL_CONFIGURATION_MANAGE')")
    @PutMapping(value = "/baseUrl",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setBaseUrl(@ApiParam(value = "The base URL", required = true)
                                     @RequestBody BaseUrlEntityBody baseUrlEntity,
                                     @RequestHeader(HttpHeaders.ACCEPT) String accept) throws IOException
    {
        try
        {
            String newBaseUrl = baseUrlEntity.getBaseUrl();
            configurationManagementService.setBaseUrl(newBaseUrl);

            logger.info("Set baseUrl to [{}].", newBaseUrl);

            return ResponseEntity.ok(getResponseEntityBody("The base URL was updated.", accept));
        }
        catch (ConfigurationException e)
        {
            String message = "Could not update the base URL of the service.";

            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Returns the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = String.class),
                            @ApiResponse(code = 404, message = "No value for baseUrl has been defined yet.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_VIEW_BASE_URL', 'GLOBAL_CONFIGURATION_MANAGE')")
    @GetMapping(value = "/baseUrl",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getBaseUrl(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        String baseUrl = configurationManagementService.getConfiguration().getBaseUrl();
        if (baseUrl != null)
        {
            return ResponseEntity.ok(getBaseUrlEntityBody(baseUrl, accept));
        }
        else
        {
            String message = "No value for baseUrl has been defined yet.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The port was updated."),
                            @ApiResponse(code = 400, message = "Could not update the strongbox port.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_SET_PORT', 'GLOBAL_CONFIGURATION_MANAGE')")
    @PutMapping(value = "/port/{port}",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setPort(@ApiParam(value = "The port of the service", required = true)
                                  @PathVariable int port,
                                  @RequestHeader(HttpHeaders.ACCEPT) String accept) throws IOException
    {
        try
        {
            configurationManagementService.setPort(port);

            logger.info("Set port to {}. This operation will require a server restart.", port);

            return ResponseEntity.ok(getResponseEntityBody("The port was updated.", accept));
        }
        catch (ConfigurationException e)
        {
            String message = "Could not update the strongbox port.";
            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The port was updated."),
                            @ApiResponse(code = 400, message = "Could not update the strongbox port.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_SET_PORT', 'GLOBAL_CONFIGURATION_MANAGE')")
    @PutMapping(value = "/port",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setPort(@ApiParam(value = "The port of the service", required = true)
                                  @RequestBody PortEntityBody portEntity,
                                  @RequestHeader(HttpHeaders.ACCEPT) String accept) throws IOException
    {
        return setPort(portEntity.getPort(), accept);
    }

    @ApiOperation(value = "Returns the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = String.class) })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_VIEW_PORT', 'GLOBAL_CONFIGURATION_MANAGE')")
    @GetMapping(value = "/port",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getPort(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        return ResponseEntity.ok(
                getPortEntityBody(configurationManagementService.getConfiguration().getPort(), accept));
    }

    @ApiOperation(value = "Set global server settings.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_SAVE_SERVER_SETTINGS),
                            @ApiResponse(code = 400, message = FAILED_SAVE_SERVER_SETTINGS) })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_SET_BASE_URL', 'CONFIGURATION_SET_PORT', 'GLOBAL_CONFIGURATION_MANAGE')")
    @PostMapping(value = "/serverSettings",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setServerSettings(@RequestBody ServerSettingsForm serverSettingsForm,
                                            BindingResult bindingResult,
                                            @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader) throws IOException
    {
        if (serverSettingsForm == null)
        {
            return getBadRequestResponseEntity(FAILED_EMPTY_FORM, acceptHeader);
        }

        validateServerSettingsForm(serverSettingsForm, bindingResult);

        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_SAVE_SERVER_SETTINGS, bindingResult);

        }

        configurationManagementService.setBaseUrl(serverSettingsForm.getBaseUrl());
        configurationManagementService.setPort(serverSettingsForm.getPort());
        configurationManagementService.setInstanceName(serverSettingsForm.getInstanceName());

        if (serverSettingsForm.getCorsConfigurationForm() != null)
        {
            configurationManagementService.setCorsAllowedOrigins(
                    serverSettingsForm.getCorsConfigurationForm().getAllowedOrigins()
            );
        }

        if (serverSettingsForm.getSmtpConfigurationForm() != null)
        {
            // SMTP settings
            configurationManagementService.setSmtpSettings(
                    serverSettingsForm.getSmtpConfigurationForm().getMutableSmtpConfiguration()
            );
        }

        if (serverSettingsForm.getProxyConfigurationForm() != null)
        {
            // Global Proxy settings
            configurationManagementService.setProxyConfiguration(
                    null, null, serverSettingsForm.getProxyConfigurationForm().getMutableProxyConfiguration()
            );
        }

        return getSuccessfulResponseEntity(SUCCESSFUL_SAVE_SERVER_SETTINGS, acceptHeader);
    }

    private void validateServerSettingsForm(ServerSettingsForm form,
                                            BindingResult bindingResult)
    {
        if (!isProxyConfigurationFormEmpty(form.getProxyConfigurationForm()))
        {
            ValidationUtils.invokeValidator(validator, form, bindingResult,
                                            ProxyConfigurationForm.ProxyConfigurationFormChecks.class);
        }

        if (!isSmtpConfigurationFormEmpty(form.getSmtpConfigurationForm()))
        {
            ValidationUtils.invokeValidator(validator, form, bindingResult,
                                            SmtpConfigurationForm.SmtpConfigurationFormChecks.class);
        }

        ValidationUtils.invokeValidator(validator, form, bindingResult);

    }

    private boolean isProxyConfigurationFormEmpty(ProxyConfigurationForm form)
    {
        return Stream.of(form.getHost(), form.getPort(), form.getType())
                     .allMatch(this::isNullOrEmpty);
    }

    private boolean isSmtpConfigurationFormEmpty(SmtpConfigurationForm form)
    {
        return Stream.of(form.getHost(), form.getPort(), form.getConnection())
                     .allMatch(this::isNullOrEmpty);
    }

    private boolean isNullOrEmpty(Object object)
    {
        if (object instanceof String)
        {
            String strObject = (String) object;
            return StringUtils.isBlank(strObject);
        }
        if (object instanceof Collection)
        {
            Collection collectionObject = (Collection) object;
            return CollectionUtils.isEmpty(collectionObject);
        }
        return Objects.isNull(object);
    }

    @ApiOperation(value = "Get global server settings.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_SAVE_SERVER_SETTINGS),
                            @ApiResponse(code = 400, message = FAILED_SAVE_SERVER_SETTINGS) })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_SET_BASE_URL', 'CONFIGURATION_SET_PORT', 'GLOBAL_CONFIGURATION_MANAGE')")
    @GetMapping(value = "/serverSettings",
                produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getServerSettings(Model model)
    {

        Configuration configuration = configurationManagementService.getConfiguration();

        ServerSettingsForm settings = new ServerSettingsForm();
        settings.setBaseUrl(configuration.getBaseUrl());
        settings.setInstanceName(configuration.getInstanceName());
        settings.setPort(configuration.getPort());
        settings.setCorsConfigurationForm(
                CorsConfigurationForm.fromConfiguration(configuration.getCorsConfiguration())
        );
        settings.setSmtpConfigurationForm(
                SmtpConfigurationForm.fromConfiguration(configuration.getSmtpConfiguration())
        );
        settings.setProxyConfigurationForm(
                ProxyConfigurationForm.fromConfiguration(configuration.getProxyConfiguration())
        );

        return ResponseEntity.ok(settings);
    }

    private Object getInstanceNameEntityBody(String instanceName,
                                             String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new InstanceNameEntityBody(instanceName);
        }
        else
        {
            return instanceName;
        }
    }

    private Object getBaseUrlEntityBody(String baseUrl,
                                        String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new BaseUrlEntityBody(baseUrl);
        }
        else
        {
            return baseUrl;
        }
    }

    private Object getPortEntityBody(int port,
                                     String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new PortEntityBody(port);
        }
        else
        {
            return String.valueOf(port);
        }
    }
}

package org.carlspring.strongbox.client;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.security.authentication.AuthenticationServiceException;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

/**
 * @author mtodorov
 */
public class ArtifactClient
        extends BaseArtifactClient
        implements Closeable
{

    private static final String HEADER_VALUE_MAVEN = "Maven/*";

    private static final String HEADER_NAME_USER_AGENT = "user-agent";

    protected String username;

    protected String password;

    private String protocol = "http";

    private String host = System.getProperty("strongbox.host") != null ?
                          System.getProperty("strongbox.host") : "localhost";

    private int port = System.getProperty("strongbox.port") != null ?
                       Integer.parseInt(System.getProperty("strongbox.port")) : 48080;

    private String contextBaseUrl;

    private Client client;


    public ArtifactClient()
    {
    }

    public static ArtifactClient getTestInstance()
    {
        return getTestInstance("deployer", "password");
    }

    public static ArtifactClient getTestInstanceLoggedInAsAdmin()
    {
        return getTestInstance("admin", "password");
    }

    public static ArtifactClient getTestInstance(String username,
                                                 String password)
    {
        String host = System.getProperty("strongbox.host") != null ?
                      System.getProperty("strongbox.host") :
                      "localhost";

        int port = System.getProperty("strongbox.port") != null ?
                   Integer.parseInt(System.getProperty("strongbox.port")) :
                   48080;

        ArtifactClient client = new ArtifactClient();
        client.setUsername(username != null ? username : "deployer");
        client.setPassword(password != null ? password : "password");
        client.setPort(port);
        client.setContextBaseUrl("http://" + host + ":" + client.getPort());

        return client;
    }

    public Client getClientInstance()
    {
        if (client == null)
        {
            ClientConfig config = getClientConfig();
            client = ClientBuilder.newClient(config);

            return client;
        }
        else
        {
            return client;
        }
    }

    private ClientConfig getClientConfig()
    {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(new ApacheConnectorProvider());

        return config;
    }

    @Override
    public void close()
    {
        if (client != null)
        {
            client.close();
        }
    }

    public void deployFile(InputStream is,
                           String url,
                           String fileName)
            throws ArtifactOperationException
    {
        put(is, url, fileName, MediaType.APPLICATION_OCTET_STREAM);
    }

    public void deployMetadata(InputStream is,
                               String url,
                               String fileName)
            throws ArtifactOperationException
    {
        put(is, url, fileName, MediaType.APPLICATION_XML);
    }

    public void put(InputStream is,
                    String url,
                    String fileName,
                    String mediaType)
            throws ArtifactOperationException
    {
        String contentDisposition = "attachment; filename=\"" + fileName + "\"";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(mediaType)
                                    .header("Content-Disposition", contentDisposition)
                                    .header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN)
                                    .put(Entity.entity(is, mediaType));

        handleFailures(response, "Failed to upload file!");
    }


    public InputStream getResource(String path,
                                   long offset)
            throws ArtifactTransportException,
                   IOException
    {
        return getResource(path, offset, null, null);
    }

    public InputStream getResource(String path,
                                   long offset,
                                   MediaType mediaType,
                                   Map<String, String> headers)
            throws ArtifactTransportException,
                   IOException
    {
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path;

        logger.debug("Getting {}...", url);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Invocation.Builder request = resource.request();
        if (mediaType != null)
        {
            resource.request(mediaType.getType());
        }

        if (headers != null)
        {
            for (Map.Entry<String, String> header : headers.entrySet())
            {
                resource.request().header(header.getKey(), header.getValue());
            }
        }

        Response response;

        if (offset > 0)
        {
            response = request.header("Range", "bytes=" + offset + "-").get();
        }
        else
        {
            response = request.get();
        }

        return response.readEntity(InputStream.class);
    }

    public Response getResourceWithResponse(String path)
            throws ArtifactTransportException,
                   IOException
    {
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path;

        logger.debug("Getting {}...", url);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        return resource.request(MediaType.TEXT_PLAIN).header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN).get();
    }

    public void delete(String storageId,
                       String repositoryId,
                       String path)
            throws ArtifactOperationException
    {
        delete(storageId, repositoryId, path, false);
    }

    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path +
                     (force ? "?force=" + force : "");

        logger.info("Getting {}...", url);
        
        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN).delete();

        handleFailures(response, "Failed to delete artifact!");
    }

    public void deleteTrash(String storageId,
                            String repositoryId)
            throws ArtifactOperationException
    {
        String url = getUrlForTrash(storageId, repositoryId);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN).delete();

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    public void deleteTrash()
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/api/trash";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN).delete();

        handleFailures(response, "Failed to delete trash for all repositories!");
    }

    public void undelete(String storageId,
                         String repositoryId,
                         String path)
            throws ArtifactOperationException
    {
        @SuppressWarnings("ConstantConditions")
        String url = getUrlForTrash(storageId, repositoryId) + "/" + path;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN)
                                    .post(Entity.entity("Undelete", MediaType.TEXT_PLAIN));

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    public void undeleteTrash(String storageId,
                              String repositoryId)
            throws ArtifactOperationException
    {
        String url = getUrlForTrash(storageId, repositoryId);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN)
                                    .post(Entity.entity("Undelete", MediaType.TEXT_PLAIN));

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    public void undeleteTrash()
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/api/trash";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN)
                                    .post(Entity.entity("Undelete", MediaType.TEXT_PLAIN));

        handleFailures(response, "Failed to delete the trash!");
    }


    public boolean pathExists(String path)
    {
        String url = escapeUrl(path);

        logger.debug("Path to artifact: {}", url);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN).header(HEADER_NAME_USER_AGENT, HEADER_VALUE_MAVEN).get();
        try
        {
            return response.getStatus() == HttpStatus.SC_OK;
        }
        finally
        {
            response.close();
        }
    }

    public void handleFailures(Response response,
                               String message)
            throws ArtifactOperationException, AuthenticationServiceException
    {

        int status = response.getStatus();

        if (status == SC_UNAUTHORIZED || status == SC_FORBIDDEN)
        {
            // TODO Handle authentication exceptions in a right way
            throw new AuthenticationServiceException(message +
                                                     "\nUser is unauthorized to execute that operation. " +
                                                     "Check assigned roles and privileges.");
        }
        else if (status != 200)
        {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("\n ERROR ").append(status).append(" ").append(message).append("\n");
            Object entity = response.getEntity();
            if (entity != null)
            {
                messageBuilder.append(entity.toString());
            }
            logger.error(messageBuilder.toString());
        }
    }

    public String getUrlForTrash(String storageId,
                                 String repositoryId)
    {
        return getContextBaseUrl() + "/api/trash/" + storageId + "/" + repositoryId;
    }

    public WebTarget setupAuthentication(WebTarget target)
    {
        if (username != null && password != null)
        {
            logger.trace("[setupAuthentication] {}", username);
            target.register(HttpAuthenticationFeature.basic(username, password));
            return target;
        }
        else
        {
            throw new ServerErrorException("Unable to setup authentication", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getContextBaseUrl()
    {
        if (contextBaseUrl == null)
        {
            contextBaseUrl = protocol + "://" + host + ":" + port;
        }

        return contextBaseUrl;
    }

    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;
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

}

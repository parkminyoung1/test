package org.carlspring.strongbox.services.impl;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.certificates.KeyStoreManager;
import org.carlspring.strongbox.services.TrustStoreService;
import org.carlspring.strongbox.services.support.TrustStoreCertificateOperationException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * @author Przemyslaw Fusik
 */
@Service
public class TrustStoreServiceImpl
        implements TrustStoreService
{

    private static final String PASSWORD = "password";

    private Resource trustStore;

    @Inject
    private KeyStoreManager keyStoreManager;

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;


    @PostConstruct
    public void init()
    throws IOException
    {
        trustStore = getTrustStoreResource();
    }

    @Override
    public void addSslCertificatesToTrustStore(String host)
            throws IOException, TrustStoreCertificateOperationException
    {
        final URL url = new URL(host);
        final String urlHost = url.getHost();
        final int urlPort = url.getPort() != -1 ? url.getPort() : url.getDefaultPort();

        try
        {
            keyStoreManager.addCertificates(Paths.get(trustStore.getURI()),
                                            PASSWORD.toCharArray(),
                                            InetAddress.getByName(urlHost),
                                            urlPort);
        }
        catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex)
        {
            throw new TrustStoreCertificateOperationException(ex);
        }
    }

    private Resource getTrustStoreResource()
    {
        return configurationResourceResolver.getConfigurationResource("strongbox.truststore.jks",
                                                                      "etc/ssl/truststore.jks");
    }

}

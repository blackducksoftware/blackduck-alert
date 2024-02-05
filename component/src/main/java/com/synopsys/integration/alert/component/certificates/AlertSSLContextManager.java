package com.synopsys.integration.alert.component.certificates;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Optional;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;

@Component
public class AlertSSLContextManager {

    private final AlertTrustStoreManager trustStoreManager;
    private final AlertClientCertificateManager clientCertificateManager;

    @Autowired
    public AlertSSLContextManager(AlertTrustStoreManager trustStoreManager, AlertClientCertificateManager clientCertificateManager) {
        this.trustStoreManager = trustStoreManager;
        this.clientCertificateManager = clientCertificateManager;
    }

    public void initialize() throws AlertConfigurationException {
    }

    public Optional<SSLContext> buildSslContext() throws AlertConfigurationException {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientCertificateManager.getClientKeyStore().orElseThrow(), "".toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStoreManager.getTrustStore());
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        } catch (AlertException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
            throw new AlertConfigurationException(ex);
        }
        return Optional.ofNullable(sslContext);
    }
}

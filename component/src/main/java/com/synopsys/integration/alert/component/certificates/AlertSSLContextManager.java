package com.synopsys.integration.alert.component.certificates;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundleKey;
import org.springframework.boot.ssl.SslManagerBundle;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;

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
            if (clientCertificateManager.getClientKeyStore().isPresent() && clientCertificateManager.getClientKeyPassword().isPresent()) {
                SslManagerBundle sslManagerBundle = getSslManagerBundle();
                sslContext = sslManagerBundle.createSslContext("TLS");
            } else {
                sslContext = SSLContext.getDefault();
            }
        } catch (AlertException | NoSuchAlgorithmException ex) {
            throw new AlertConfigurationException(ex);
        }
        return Optional.ofNullable(sslContext);
    }

    @NotNull
    private SslManagerBundle getSslManagerBundle() throws AlertException {
        KeyStore trustStore = trustStoreManager.getTrustStore();
        // when the client keystore is created the password is changed to null by the implementation class used to read the certificate and key data.
        // pass null in order to create the manager bundle.
        KeyStore clientKeystore = clientCertificateManager.getClientKeyStore().orElse(null);
        SslBundleKey sslBundleKey = SslBundleKey.of(null, AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);
        SslStoreBundle sslStoreBundle = SslStoreBundle.of(clientKeystore, null, trustStore);
        return SslManagerBundle.from(sslStoreBundle, sslBundleKey);
    }
}

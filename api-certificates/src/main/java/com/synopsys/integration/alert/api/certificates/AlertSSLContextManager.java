package com.synopsys.integration.alert.api.certificates;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundleKey;
import org.springframework.boot.ssl.SslManagerBundle;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
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

    public Optional<SSLContext> buildSslContext() throws AlertConfigurationException {
        SSLContext sslContext;
        try {
            Optional<SslManagerBundle> sslManagerBundle = getSslManagerBundle();
            if (sslManagerBundle.isPresent()) {
                sslContext = sslManagerBundle.get().createSslContext("TLS");
            } else {
                sslContext = SSLContext.getDefault();
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new AlertConfigurationException(ex);
        }
        return Optional.ofNullable(sslContext);
    }

    private Optional<SslManagerBundle> getSslManagerBundle() {
        Optional<KeyStore> trustStore = trustStoreManager.getTrustStore();
        if (clientCertificateManager.containsClientCertificate() && trustStore.isPresent()) {
            // when the client keystore is created the password is changed to null by the implementation class used to read the certificate and key data.
            // pass null in order to create the manager bundle.
            KeyStore clientKeystore = clientCertificateManager.getClientKeyStore().orElse(null);
            SslBundleKey sslBundleKey = SslBundleKey.of(null, AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS);
            SslStoreBundle sslStoreBundle = SslStoreBundle.of(clientKeystore, null, trustStore.orElse(null));
            return Optional.of(SslManagerBundle.from(sslStoreBundle, sslBundleKey));
        } else {
            return Optional.empty();
        }
    }
}

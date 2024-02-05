package com.synopsys.integration.alert.component.certificates;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlertSSLContextManager {

    private final AlertTrustStoreManager trustStoreManager;
    private final AlertClientCertificateManager clientCertificateManager;

    @Autowired
    public AlertSSLContextManager(AlertTrustStoreManager trustStoreManager, AlertClientCertificateManager clientCertificateManager) {
        this.trustStoreManager = trustStoreManager;
        this.clientCertificateManager = clientCertificateManager;
    }

    public void initialize() {
    }

    public SSLContext buildSslContext() throws NoSuchAlgorithmException {
        return SSLContext.getDefault();
    }
}

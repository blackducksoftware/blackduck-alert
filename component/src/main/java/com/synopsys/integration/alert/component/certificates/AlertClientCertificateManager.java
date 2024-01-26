package com.synopsys.integration.alert.component.certificates;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.ClientCertificateModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;

@Component
public class AlertClientCertificateManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AlertProperties alertProperties;
    private final KeyStoreManager keyStoreManager;

    @Autowired
    public AlertClientCertificateManager(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
        this.keyStoreManager = KeyStoreManager.of(getKeyStoreType());
    }

    public synchronized void importCertificate(ClientCertificateModel clientCertificateModel) throws AlertException {
        logger.debug("Importing certificate into key store.");
        validateClientCertificateHasValues(clientCertificateModel);
        File trustStoreFile = getAndValidateKeyStoreFile();
        Certificate cert = keyStoreManager.getAsJavaCertificate(clientCertificateModel.getCertificateContent());

        keyStoreManager.importCertificate(cert, AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS, trustStoreFile, getKeyStorePassword());
    }

    public synchronized void removeCertificate() throws AlertException {
        logger.debug("Removing certificate from key store.");
        Optional<String> optionalTrustStoreFileName = alertProperties.getMtlsClientKeyStoreFile();
        if (optionalTrustStoreFileName.isPresent()) {
            File trustStoreFile = getAndValidateKeyStoreFile();
            KeyStore trustStore = keyStoreManager.getAsKeyStore(trustStoreFile, getKeyStorePassword());
            keyStoreManager.removeCertificate(AlertRestConstants.DEFAULT_CLIENT_CERTIFICATE_ALIAS, optionalTrustStoreFileName.get(), trustStore, getKeyStorePassword());
        } else {
            throw new AlertConfigurationException("No trust store file has been provided.");
        }
    }

    private void validateClientCertificateHasValues(ClientCertificateModel clientCertificateModel) throws AlertException {
        if (null == clientCertificateModel) {
            throw new AlertException("The custom certificate cannot be null.");
        }

        if (StringUtils.isBlank(clientCertificateModel.getCertificateContent())) {
            throw new AlertException("The certificate content cannot be blank.");
        }
    }

    public synchronized File getAndValidateKeyStoreFile() throws AlertConfigurationException {
        Optional<String> optionalKeyStoreFileName = alertProperties.getMtlsClientKeyStoreFile();
        if (optionalKeyStoreFileName.isPresent()) {
            return keyStoreManager.getAndValidateKeyStoreFile(optionalKeyStoreFileName.get());
        } else {
            throw new AlertConfigurationException("No trust store file has been provided.");
        }
    }

    private String getKeyStoreType() {
        return alertProperties.getMtlsClientKeyStoreType().orElse(KeyStore.getDefaultType());
    }

    private char[] getKeyStorePassword() {
        return alertProperties
            .getMtlsClientKeyStorePass()
            .map(String::toCharArray)
            .orElse(null);
    }
}

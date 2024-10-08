package com.blackduck.integration.alert.api.certificates;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.persistence.model.CustomCertificateModel;

@Component
public class AlertTrustStoreManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AlertProperties alertProperties;
    private final KeyStoreManager keyStoreManager;

    @Autowired
    public AlertTrustStoreManager(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
        this.keyStoreManager = KeyStoreManager.of(getTrustStoreType());
    }

    public synchronized void importCertificate(CustomCertificateModel customCertificate) throws AlertException {
        logger.debug("Importing certificate into trust store.");
        validateCustomCertificateHasValues(customCertificate);
        File trustStoreFile = getAndValidateTrustStoreFile();
        Certificate cert = getAsJavaCertificate(customCertificate);

        keyStoreManager.importCertificate(cert, customCertificate.getAlias(), trustStoreFile, getTrustStorePassword());
    }

    public synchronized void removeCertificate(CustomCertificateModel customCertificate) throws AlertException {
        logger.debug("Removing certificate from trust store.");
        if (null == customCertificate) {
            throw new AlertException("The alias could not be determined from the custom certificate because it was null.");
        }
        removeCertificate(customCertificate.getAlias());
    }

    public synchronized void removeCertificate(String certificateAlias) throws AlertException {
        Optional<String> optionalTrustStoreFileName = alertProperties.getTrustStoreFile();
        if (optionalTrustStoreFileName.isPresent()) {
            File trustStoreFile = getAndValidateTrustStoreFile();
            KeyStore trustStore = keyStoreManager.getAsKeyStore(trustStoreFile, getTrustStorePassword());
            keyStoreManager.removeCertificate(certificateAlias, optionalTrustStoreFileName.get(), trustStore, getTrustStorePassword());
        } else {
            throw new AlertConfigurationException("No trust store file has been provided.");
        }
    }

    public synchronized File getAndValidateTrustStoreFile() throws AlertConfigurationException {
        Optional<String> optionalTrustStoreFileName = alertProperties.getTrustStoreFile();
        if (optionalTrustStoreFileName.isPresent()) {
            return keyStoreManager.getAndValidateKeyStoreFile(optionalTrustStoreFileName.get());
        } else {
            throw new AlertConfigurationException("No trust store file has been provided.");
        }
    }

    public synchronized Optional<KeyStore> getTrustStore() {
        Optional<String> optionalTrustStoreFileName = alertProperties.getTrustStoreFile();
        if (optionalTrustStoreFileName.isPresent()) {
            try {
                return Optional.of(keyStoreManager.getAsKeyStore(keyStoreManager.getAndValidateKeyStoreFile(optionalTrustStoreFileName.get()), getTrustStorePassword()));
            } catch (AlertException ex) {
                logger.error("Error getting trust store", ex);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }

    }

    public synchronized void validateCertificateContent(CustomCertificateModel customCertificateModel) throws AlertException {
        // Result is ignored, but we continue to throw an AlertException if one occurs
        getAsJavaCertificate(customCertificateModel);
    }

    private void validateCustomCertificateHasValues(CustomCertificateModel customCertificate) throws AlertException {
        if (null == customCertificate) {
            throw new AlertException("The custom certificate cannot be null.");
        }

        if (StringUtils.isBlank(customCertificate.getAlias())) {
            throw new AlertException("The alias cannot be blank.");
        }

        if (StringUtils.isBlank(customCertificate.getCertificateContent())) {
            throw new AlertException("The certificate content cannot be blank.");
        }
    }

    private Certificate getAsJavaCertificate(CustomCertificateModel customCertificate) throws AlertException {
        return keyStoreManager.getAsJavaCertificate(customCertificate.getCertificateContent());
    }

    private String getTrustStoreType() {
        return alertProperties.getTrustStoreType().orElse(KeyStore.getDefaultType());
    }

    private char[] getTrustStorePassword() {
        return alertProperties
            .getTrustStorePass()
            .map(String::toCharArray)
            .orElse(null);
    }
}

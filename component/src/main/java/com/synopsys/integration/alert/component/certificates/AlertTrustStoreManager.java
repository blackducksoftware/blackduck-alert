/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.certificates;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;

@Component
public class AlertTrustStoreManager {
    private final Logger logger = LoggerFactory.getLogger(AlertTrustStoreManager.class);
    private final AlertProperties alertProperties;

    @Autowired
    public AlertTrustStoreManager(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    public synchronized void importCertificate(CustomCertificateModel customCertificate) throws AlertException {
        logger.debug("Importing certificate into trust store.");
        validateCustomCertificateHasValues(customCertificate);
        File trustStoreFile = getAndValidateTrustStoreFile();
        KeyStore trustStore = getAsKeyStore(trustStoreFile, getTrustStorePassword(), getTrustStoreType());

        try {
            Certificate cert = getAsJavaCertificate(customCertificate);
            trustStore.setCertificateEntry(customCertificate.getAlias(), cert);
            try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStoreFile))) {
                trustStore.store(stream, getTrustStorePassword());
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AlertException("There was a problem storing the certificate.", e);
        }
    }

    public synchronized void removeCertificate(CustomCertificateModel customCertificate) throws AlertException {
        logger.debug("Removing certificate from trust store.");
        if (null == customCertificate) {
            throw new AlertException("The alias could not be determined from the custom certificate because it was null.");
        }
        removeCertificate(customCertificate.getAlias());
    }

    public synchronized void removeCertificate(String certificateAlias) throws AlertException {
        logger.debug("Removing certificate by alias from trust store.");
        if (StringUtils.isBlank(certificateAlias)) {
            throw new AlertException("The alias cannot be blank");
        }

        try {
            File trustStore = getAndValidateTrustStoreFile();
            KeyStore keyStore = getAsKeyStore(trustStore, getTrustStorePassword(), getTrustStoreType());
            if (keyStore.containsAlias(certificateAlias)) {
                keyStore.deleteEntry(certificateAlias);
                try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
                    keyStore.store(stream, getTrustStorePassword());
                }
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AlertException("There was a problem removing the certificate.", e);
        }
    }

    public synchronized KeyStore getAsKeyStore(File keyStore, char[] keyStorePass, String keyStoreType) throws AlertException {
        logger.debug("Get key store.");
        KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(keyStorePass);
        try {
            return KeyStore.Builder.newInstance(keyStoreType, null, keyStore, protection).getKeyStore();
        } catch (KeyStoreException e) {
            throw new AlertException("There was a problem accessing the trust store.", e);
        }
    }

    public synchronized File getAndValidateTrustStoreFile() throws AlertConfigurationException {
        logger.debug("Get and validate trust store.");
        Optional<String> optionalTrustStoreFileName = alertProperties.getTrustStoreFile();
        if (optionalTrustStoreFileName.isPresent()) {
            String trustStoreFileName = optionalTrustStoreFileName.get();
            File trustStoreFile;
            try {
                URI trustStoreUri = new URI(trustStoreFileName);
                trustStoreFile = new File(trustStoreUri);
            } catch (IllegalArgumentException | URISyntaxException ex) {
                logger.debug("Error getting Java trust store from file URI", ex);
                trustStoreFile = new File(trustStoreFileName);
            }

            if (!trustStoreFile.isFile()) {
                throw new AlertConfigurationException("The trust store provided is not a file: " + trustStoreFileName);
            }

            if (!trustStoreFile.canWrite()) {
                throw new AlertConfigurationException("The trust store provided cannot be written by Alert: " + trustStoreFileName);
            }

            return trustStoreFile;

        } else {
            throw new AlertConfigurationException("No trust store file has been provided.");
        }
    }

    public synchronized void validateCertificateContent(CustomCertificateModel customCertificateModel) throws AlertException {
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
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            String certificateContent = customCertificate.getCertificateContent();
            try (ByteArrayInputStream certInputStream = new ByteArrayInputStream(certificateContent.getBytes())) {
                return certFactory.generateCertificate(certInputStream);
            }
        } catch (CertificateException | IOException e) {
            throw new AlertException("The custom certificate could not be read.", e);
        }
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

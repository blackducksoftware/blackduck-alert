package com.synopsys.integration.alert.api.certificates;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;

public class KeyStoreManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String keyStoreType;

    public static KeyStoreManager of(String keyStoreType) {
        return new KeyStoreManager(keyStoreType);
    }

    private KeyStoreManager(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public void importCertificate(Certificate certificate, String certificateAlias, File keyStoreFile, char[] keyStorePassword) throws AlertException {
        KeyStore keyStore = getAsKeyStore(keyStoreFile, keyStorePassword);
        try {
            keyStore.setCertificateEntry(certificateAlias, certificate);
            try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(keyStoreFile))) {
                keyStore.store(stream, keyStorePassword);
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AlertException("There was a problem storing the certificate.", e);
        }
    }

    public void removeCertificate(String certificateAlias, String keyStoreFileName, KeyStore keyStore, char[] keyStorePassword) throws AlertException {
        logger.debug("Removing certificate by alias from key store.");
        if (StringUtils.isBlank(certificateAlias)) {
            throw new AlertException("The alias cannot be blank");
        }

        try {
            File trustStore = getAndValidateKeyStoreFile(keyStoreFileName);
            if (keyStore.containsAlias(certificateAlias)) {
                keyStore.deleteEntry(certificateAlias);
                try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
                    keyStore.store(stream, keyStorePassword);
                }
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AlertException("There was a problem removing the certificate.", e);
        }
    }

    public KeyStore getAsKeyStore(File keyStore, char[] keyStorePass) throws AlertException {
        logger.debug("Get key store.");
        KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(keyStorePass);
        try {
            return KeyStore.Builder.newInstance(keyStoreType, null, keyStore, protection).getKeyStore();
        } catch (KeyStoreException e) {
            throw new AlertException("There was a problem accessing the trust store.", e);
        }
    }

    public File getAndValidateKeyStoreFile(String keyStoreFileName) throws AlertConfigurationException {
        logger.debug("Get and validate trust store.");
        File trustStoreFile;
        try {
            URI trustStoreUri = new URI(keyStoreFileName);
            trustStoreFile = new File(trustStoreUri);
        } catch (IllegalArgumentException | URISyntaxException ex) {
            logger.debug("Error getting Java trust store from file URI", ex);
            trustStoreFile = new File(keyStoreFileName);
        }

        if (!trustStoreFile.isFile()) {
            throw new AlertConfigurationException("The key store provided is not a file: " + keyStoreFileName);
        }

        if (!trustStoreFile.canWrite()) {
            throw new AlertConfigurationException("The key store provided cannot be written by Alert: " + keyStoreFileName);
        }

        return trustStoreFile;
    }

    public Certificate getAsJavaCertificate(String certificateContent) throws AlertException {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            try (ByteArrayInputStream certInputStream = new ByteArrayInputStream(certificateContent.getBytes())) {
                return certFactory.generateCertificate(certInputStream);
            }
        } catch (CertificateException | IOException e) {
            throw new AlertException("The custom certificate could not be read.", e);
        }
    }
}

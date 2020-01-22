package com.synopsys.integration.alert.common.security;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;

@Component
public class CertificateUtility {
    private AlertProperties alertProperties;

    @Autowired
    public CertificateUtility(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    public void importCertificate(CustomCertificateModel customCertificate) throws AlertException {
        File trustStoreFile = getAndValidateTrustStoreFile();
        KeyStore trustStore = getAsKeyStore(trustStoreFile, getTrustStorePassword(), getTrustStoreType());

        // FIXME construct the certificate
        Certificate cert = null;

        try {
            trustStore.setCertificateEntry(customCertificate.getAlias(), cert);
            try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStoreFile))) {
                trustStore.store(stream, getTrustStorePassword());
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AlertException("There was a problem storing the certificate", e);
        }
    }

    public KeyStore getAsKeyStore(File keyStore, char[] keyStorePass, String keyStoreType) throws AlertException {
        KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(getTrustStorePassword());
        try {
            return KeyStore.Builder.newInstance(getTrustStoreType(), null, keyStore, protection).getKeyStore();
        } catch (KeyStoreException e) {
            throw new AlertException("There was a problem accessing the trust store", e);
        }
    }

    public File getAndValidateTrustStoreFile() throws AlertConfigurationException {
        Optional<String> optionalTrustStoreFileName = alertProperties.getTrustStoreFile();
        if (optionalTrustStoreFileName.isPresent()) {
            File trustStoreFile = new File(optionalTrustStoreFileName.get());
            if (!trustStoreFile.isFile()) {
                throw new AlertConfigurationException("The trust store provided is not a file");
            }

            if (trustStoreFile.canWrite()) {
                throw new AlertConfigurationException("The trust store provided cannot be written by Alert");
            }

            return trustStoreFile;
        } else {
            throw new AlertConfigurationException("No trust store file has been provided");
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

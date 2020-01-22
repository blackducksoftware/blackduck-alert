/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.security;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
        validateCustomCertificate(customCertificate);
        File trustStoreFile = getAndValidateTrustStoreFile();
        KeyStore trustStore = getAsKeyStore(trustStoreFile, getTrustStorePassword(), getTrustStoreType());

        try {
            Certificate cert = getAsJavaCertificate(customCertificate);
            trustStore.setCertificateEntry(customCertificate.getAlias(), cert);
            try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStoreFile))) {
                trustStore.store(stream, getTrustStorePassword());
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AlertException("There was a problem storing the certificate", e);
        }
    }

    public void removeCertificate(CustomCertificateModel customCertificate) throws AlertException {
        if (null == customCertificate) {
            throw new AlertException("The alias could not be determined from the custom certificate because it was null");
        }
        removeCertificate(customCertificate.getAlias());
    }

    public void removeCertificate(String certificateAlias) throws AlertException {
        if (StringUtils.isBlank(certificateAlias)) {
            throw new AlertException("The alias cannot be blank");
        }
        File trustStore = getAndValidateTrustStoreFile();
        KeyStore keyStore = getAsKeyStore(trustStore, getTrustStorePassword(), getTrustStoreType());
        try {
            if (keyStore.containsAlias(certificateAlias)) {
                keyStore.deleteEntry(certificateAlias);
                try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(trustStore))) {
                    keyStore.store(stream, getTrustStorePassword());
                }
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AlertException("There was a problem removing the certificate", e);
        }
    }

    public KeyStore getAsKeyStore(File keyStore, char[] keyStorePass, String keyStoreType) throws AlertException {
        KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(keyStorePass);
        try {
            return KeyStore.Builder.newInstance(keyStoreType, null, keyStore, protection).getKeyStore();
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

    private void validateCustomCertificate(CustomCertificateModel customCertificate) throws AlertException {
        if (null == customCertificate) {
            throw new AlertException("The custom certificate cannot be null");
        }

        if (StringUtils.isBlank(customCertificate.getAlias())) {
            throw new AlertException("The alias cannot be blank");
        }

        if (StringUtils.isBlank(customCertificate.getCertificateContent())) {
            throw new AlertException("The certificate content cannot be blank");
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
            throw new AlertException("The custom certificate could not be read", e);
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

/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.FilePersistenceUtil;

@Component
public class EncryptionUtility {
    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtility.class);
    private static final String DATA_FILE_NAME = "alert_encryption_data.json";
    private final AlertProperties alertProperties;
    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public EncryptionUtility(final AlertProperties alertProperties, final FilePersistenceUtil filePersistenceUtil) {
        this.alertProperties = alertProperties;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public String encrypt(final String value) {
        final TextEncryptor encryptor = Encryptors.delux(getPassword(), getEncodedSalt());
        return encryptor.encrypt(value);
    }

    public String decrypt(final String encryptedValue) {
        String decryptedValue = "";
        try {
            final TextEncryptor decryptor = Encryptors.delux(getPassword(), getEncodedSalt());
            decryptedValue = decryptor.decrypt(encryptedValue);
        } catch (final IllegalArgumentException | NullPointerException ex) {
            logger.error("Error decrypting value", ex);
        }
        return decryptedValue;
    }

    public boolean isInitialized() {
        return isPasswordSet() && isGlobalSaltSet();
    }

    public void updateEncryptionFields(final String password, final String globalSalt) throws IOException {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Encryption password cannot be blank");
        }

        if (StringUtils.isBlank(globalSalt)) {
            throw new IllegalArgumentException("Encryption global salt cannot be blank");
        }
        final EncryptionFileData encryptionFileData = new EncryptionFileData(password, globalSalt);
        filePersistenceUtil.writeJsonToFile(DATA_FILE_NAME, encryptionFileData);
    }

    private String getEncodedSalt() {
        if (isInitialized()) {
            final byte[] saltBytes = getGlobalSalt().getBytes(Charsets.UTF_8);
            return Hex.encodeHexString(saltBytes);
        } else {
            return null;
        }
    }

    public boolean isPasswordSet() {
        return null != getPassword();
    }

    private String getPassword() {
        final Optional<String> passwordFromEnvironment = alertProperties.getAlertEncryptionPassword();
        return passwordFromEnvironment.orElse(getPasswordFromFile());
    }

    private String getPasswordFromFile() {
        try {
            final EncryptionFileData encryptionFileData = filePersistenceUtil.readJsonFromFile(DATA_FILE_NAME, EncryptionFileData.class);
            return encryptionFileData.getPassword();
        } catch (final IOException ex) {
            logger.debug("Error getting password from file.", ex);
            return null;
        }
    }

    public boolean isGlobalSaltSet() {
        return null != getGlobalSalt();
    }

    private String getGlobalSalt() {
        final Optional<String> saltFromEnvironment = alertProperties.getAlertEncryptionGlobalSalt();
        return saltFromEnvironment.orElse(getGlobalSaltFromFile());
    }

    private String getGlobalSaltFromFile() {
        try {
            final EncryptionFileData encryptionFileData = filePersistenceUtil.readJsonFromFile(DATA_FILE_NAME, EncryptionFileData.class);
            return encryptionFileData.getGlobalSalt();
        } catch (final IOException ex) {
            logger.debug("Error getting password from file.", ex);
            return null;
        }
    }

    private class EncryptionFileData {
        private final String password;
        private final String globalSalt;

        private EncryptionFileData(final String password, final String globalSalt) {
            this.password = password;
            this.globalSalt = globalSalt;
        }

        public String getPassword() {
            return password;
        }

        public String getGlobalSalt() {
            return globalSalt;
        }
    }
}

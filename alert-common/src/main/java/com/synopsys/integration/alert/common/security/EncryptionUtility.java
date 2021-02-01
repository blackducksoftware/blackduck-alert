/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;

@Component
public class EncryptionUtility {
    private final Logger logger = LoggerFactory.getLogger(EncryptionUtility.class);
    private static final String DATA_FILE_NAME = "alert_encryption_data.json";
    private static final String SECRETS_ENCRYPTION_PASSWORD = "ALERT_ENCRYPTION_PASSWORD";
    private static final String SECRETS_ENCRYPTION_SALT = "ALERT_ENCRYPTION_GLOBAL_SALT";
    private final AlertProperties alertProperties;
    private final FilePersistenceUtil filePersistenceUtil;

    @Autowired
    public EncryptionUtility(AlertProperties alertProperties, FilePersistenceUtil filePersistenceUtil) {
        this.alertProperties = alertProperties;
        this.filePersistenceUtil = filePersistenceUtil;
    }

    public String encrypt(String value) {
        String password = getPassword();
        String salt = getEncodedSalt();
        if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(salt)) {
            TextEncryptor encryptor = Encryptors.delux(password, salt);
            return encryptor.encrypt(value);
        }
        return StringUtils.EMPTY;
    }

    public String decrypt(String encryptedValue) {
        try {
            String password = getPassword();
            String salt = getEncodedSalt();
            if (StringUtils.isNotBlank(encryptedValue) && StringUtils.isNotBlank(password) && StringUtils.isNotBlank(salt)) {
                TextEncryptor decryptor = Encryptors.delux(password, salt);
                return decryptor.decrypt(encryptedValue);
            }
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException ex) {
            logger.error("Error decrypting value", ex);
        }
        return StringUtils.EMPTY;
    }

    public boolean isInitialized() {
        return isPasswordSet() && isGlobalSaltSet();
    }

    public void updatePasswordField(String password) throws IOException {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Encryption password cannot be blank");
        }

        EncryptionFileData encryptionFileData = new EncryptionFileData(password, getGlobalSalt());
        filePersistenceUtil.writeJsonToFile(DATA_FILE_NAME, encryptionFileData);
    }

    public void updateSaltField(String globalSalt) throws IOException {
        if (StringUtils.isBlank(globalSalt)) {
            throw new IllegalArgumentException("Encryption global salt cannot be blank");
        }
        EncryptionFileData encryptionFileData = new EncryptionFileData(getPassword(), globalSalt);
        filePersistenceUtil.writeJsonToFile(DATA_FILE_NAME, encryptionFileData);
    }

    public void updateEncryptionFields(String password, String globalSalt) throws IOException {
        updatePasswordField(password);
        updateSaltField(globalSalt);
    }

    private String getEncodedSalt() {
        if (isInitialized()) {
            byte[] saltBytes = getGlobalSalt().getBytes(Charsets.UTF_8);
            return Hex.encodeHexString(saltBytes);
        } else {
            return null;
        }
    }

    public boolean isPasswordSet() {
        return StringUtils.isNotBlank(getPassword());
    }

    public boolean isPasswordMissing() {
        return StringUtils.isBlank(getPassword());
    }

    private String getPassword() {
        Optional<String> passwordFromEnvironment = alertProperties.getAlertEncryptionPassword();
        return passwordFromEnvironment.orElseGet(this::getPasswordFromFile);
    }

    private String getPasswordFromFile() {
        Optional<String> passwordFromSecrets = readPasswordFromSecretsFile();
        return passwordFromSecrets.orElseGet(this::readPasswordFromVolumeDataFile);
    }

    private Optional<String> readPasswordFromSecretsFile() {
        try {
            return Optional.ofNullable(filePersistenceUtil.readFromSecretsFile(SECRETS_ENCRYPTION_PASSWORD));
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    private String readPasswordFromVolumeDataFile() {
        try {
            EncryptionFileData encryptionFileData = filePersistenceUtil.readJsonFromFile(DATA_FILE_NAME, EncryptionFileData.class);
            return encryptionFileData.getPassword();
        } catch (IOException ex) {
            logger.debug("Error getting password from volume data file: {}", ex.getLocalizedMessage());
            return null;
        }
    }

    public boolean isGlobalSaltSet() {
        return StringUtils.isNotBlank(getGlobalSalt());
    }

    public boolean isGlobalSaltMissing() {
        return StringUtils.isBlank(getGlobalSalt());
    }

    private String getGlobalSalt() {
        Optional<String> saltFromEnvironment = alertProperties.getAlertEncryptionGlobalSalt();
        return saltFromEnvironment.orElseGet(this::getGlobalSaltFromFile);
    }

    private String getGlobalSaltFromFile() {
        Optional<String> saltFromSecrets = readGlobalSaltFromSecretsFile();
        return saltFromSecrets.orElseGet(this::readGlobalSaltFromVolumeDataFile);
    }

    private Optional<String> readGlobalSaltFromSecretsFile() {
        try {
            return Optional.ofNullable(filePersistenceUtil.readFromSecretsFile(SECRETS_ENCRYPTION_SALT));
        } catch (IOException ex) {
            logger.debug("Error getting new global salt file: {}", ex.getLocalizedMessage());
        }
        return Optional.empty();
    }

    private String readGlobalSaltFromVolumeDataFile() {
        try {
            EncryptionFileData encryptionFileData = filePersistenceUtil.readJsonFromFile(DATA_FILE_NAME, EncryptionFileData.class);
            return encryptionFileData.getGlobalSalt();
        } catch (IOException ex) {
            logger.debug("Error getting global salt from volume data file: {}", ex.getLocalizedMessage());
            return null;
        }
    }

    private class EncryptionFileData {
        private final String password;
        private final String globalSalt;

        private EncryptionFileData(String password, String globalSalt) {
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

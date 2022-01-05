/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.security;

import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
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

    public boolean isEncryptionFromEnvironment() {
        return isPasswordFromEnvironment() || isGlobalSaltFromEnvironment();
    }

    public void updatePasswordFieldInVolumeDataFile(String password) throws IOException {
        if (StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Encryption password cannot be blank");
        }
        EncryptionFileData encryptionFileData = new EncryptionFileData(password, readGlobalSaltFromVolumeDataFile());
        filePersistenceUtil.writeJsonToFile(DATA_FILE_NAME, encryptionFileData);
    }

    public void updateSaltFieldInVolumeDataFile(String globalSalt) throws IOException {
        if (StringUtils.isBlank(globalSalt)) {
            throw new IllegalArgumentException("Encryption global salt cannot be blank");
        }
        EncryptionFileData encryptionFileData = new EncryptionFileData(readPasswordFromVolumeDataFile(), globalSalt);
        filePersistenceUtil.writeJsonToFile(DATA_FILE_NAME, encryptionFileData);
    }

    public void updateEncryptionFieldsInVolumeDataFile(String password, String globalSalt) throws IOException {
        updatePasswordFieldInVolumeDataFile(password);
        updateSaltFieldInVolumeDataFile(globalSalt);
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

    public boolean isPasswordFromEnvironment() {
        return alertProperties.getAlertEncryptionPassword().or(this::readPasswordFromSecretsFile).isPresent();
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

    public boolean isGlobalSaltFromEnvironment() {
        return alertProperties.getAlertEncryptionGlobalSalt().or(this::readGlobalSaltFromSecretsFile).isPresent();
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
            logger.trace("Error getting new global salt file: {}", ex.getLocalizedMessage());
        }
        return Optional.empty();
    }

    private String readGlobalSaltFromVolumeDataFile() {
        try {
            EncryptionFileData encryptionFileData = filePersistenceUtil.readJsonFromFile(DATA_FILE_NAME, EncryptionFileData.class);
            return encryptionFileData.getGlobalSalt();
        } catch (IOException ex) {
            logger.trace("Error getting global salt from volume data file: {}", ex.getLocalizedMessage());
            return null;
        }
    }

    private static class EncryptionFileData implements Serializable {
        private static final long serialVersionUID = -2810887223126346010L;
        @Nullable
        private final String password;
        @Nullable
        private final String globalSalt;

        private EncryptionFileData(@Nullable String password, @Nullable String globalSalt) {
            this.password = password;
            this.globalSalt = globalSalt;
        }

        public @Nullable String getPassword() {
            return password;
        }

        public @Nullable String getGlobalSalt() {
            return globalSalt;
        }

    }

}

/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.encryption.model;

import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.api.common.model.Obfuscated;

public class SettingsEncryptionModel extends AlertSerializableModel implements Obfuscated<SettingsEncryptionModel> {
    private Boolean isEncryptionPasswordSet;
    private String encryptionPassword;
    private Boolean isEncryptionGlobalSaltSet;
    private String encryptionGlobalSalt;
    private boolean readOnly;

    public SettingsEncryptionModel() {
        // For serialization
    }

    public SettingsEncryptionModel(Boolean isEncryptionPasswordSet, Boolean isEncryptionGlobalSaltSet, boolean readOnly) {
        this.isEncryptionPasswordSet = isEncryptionPasswordSet;
        this.isEncryptionGlobalSaltSet = isEncryptionGlobalSaltSet;
        this.readOnly = readOnly;
    }

    public SettingsEncryptionModel(String encryptionPassword, Boolean isEncryptionPasswordSet, String encryptionGlobalSalt, Boolean isEncryptionGlobalSaltSet, boolean readOnly) {
        this(isEncryptionPasswordSet, isEncryptionGlobalSaltSet, readOnly);
        this.encryptionPassword = encryptionPassword;
        this.encryptionGlobalSalt = encryptionGlobalSalt;
    }

    @Override
    public SettingsEncryptionModel obfuscate() {
        // Encryption password and global salt should never be listed after obfuscating, those values will be accessed by the EncryptionUtility when needed.
        return new SettingsEncryptionModel(isEncryptionPasswordSet, isEncryptionGlobalSaltSet, readOnly);
    }

    public Boolean getIsEncryptionPasswordSet() {
        return isEncryptionPasswordSet;
    }

    public void setIsEncryptionPasswordSet(Boolean isEncryptionPasswordSet) {
        this.isEncryptionPasswordSet = isEncryptionPasswordSet;
    }

    public Optional<String> getEncryptionPassword() {
        return Optional.ofNullable(encryptionPassword);
    }

    public void setEncryptionPassword(String encryptionPassword) {
        this.encryptionPassword = encryptionPassword;
    }

    public Boolean getIsEncryptionGlobalSaltSet() {
        return isEncryptionGlobalSaltSet;
    }

    public void setIsEncryptionGlobalSaltSet(Boolean isEncryptionGlobalSaltSet) {
        this.isEncryptionGlobalSaltSet = isEncryptionGlobalSaltSet;
    }

    public Optional<String> getEncryptionGlobalSalt() {
        return Optional.ofNullable(encryptionGlobalSalt);
    }

    public void setEncryptionGlobalSalt(String encryptionGlobalSalt) {
        this.encryptionGlobalSalt = encryptionGlobalSalt;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}

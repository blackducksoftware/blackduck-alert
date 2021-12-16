/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.encryption.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.model.Obfuscated;

public class SettingsEncryptionModel extends AlertSerializableModel implements Obfuscated<SettingsEncryptionModel> {
    private Boolean isEncryptionPasswordSet;
    private String encryptionPassword;
    private Boolean isEncryptionGlobalSaltSet;
    private String encryptionGlobalSalt;
    private boolean readOnly;

    public SettingsEncryptionModel() {
        // For serialization
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

    @Override
    public SettingsEncryptionModel obfuscate() {
        SettingsEncryptionModel settingsEncryptionModel = new SettingsEncryptionModel();

        settingsEncryptionModel.setIsEncryptionPasswordSet(StringUtils.isNotBlank(encryptionPassword));
        settingsEncryptionModel.setEncryptionPassword(null);
        settingsEncryptionModel.setIsEncryptionGlobalSaltSet(StringUtils.isNotBlank(encryptionGlobalSalt));
        settingsEncryptionModel.setEncryptionGlobalSalt(null);
        settingsEncryptionModel.setReadOnly(readOnly);

        return settingsEncryptionModel;
    }
}

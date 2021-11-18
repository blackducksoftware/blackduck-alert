/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.encryption.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.rest.model.Obfuscated;

public class SettingsEncryptionModel extends AlertSerializableModel implements Obfuscated<SettingsEncryptionModel> {
    @JsonProperty("encryptionPassword")
    private String password;
    @JsonProperty("encryptionGlobalSalt")
    private String globalSalt;
    @JsonProperty("readOnly")
    private boolean readOnly;

    public SettingsEncryptionModel() {
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<String> getGlobalSalt() {
        return Optional.ofNullable(globalSalt);
    }

    public void setGlobalSalt(String globalSalt) {
        this.globalSalt = globalSalt;
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

        String maskedPassword = (password != null) ? ConfigurationCrudHelper.MASKED_VALUE : null;
        settingsEncryptionModel.setPassword(maskedPassword);
        String maskedGlobalSalt = (globalSalt != null) ? ConfigurationCrudHelper.MASKED_VALUE : null;
        settingsEncryptionModel.setGlobalSalt(maskedGlobalSalt);
        settingsEncryptionModel.setReadOnly(readOnly);

        return settingsEncryptionModel;
    }
}

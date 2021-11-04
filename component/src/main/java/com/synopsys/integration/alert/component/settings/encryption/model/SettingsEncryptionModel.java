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
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;

public class SettingsEncryptionModel extends ConfigWithMetadata {
    @JsonProperty("encryptionPassword")
    private String password;
    @JsonProperty("encryptionGlobalSalt")
    private String globalSalt;

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

}

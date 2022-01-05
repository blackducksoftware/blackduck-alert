/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.component.settings.validator.SettingsGlobalConfigurationFieldModelValidator;

@Component
public class SettingsDescriptor extends ComponentDescriptor {
    public static final String SETTINGS_LABEL = "Settings";
    public static final String SETTINGS_URL = "settings";
    public static final String SETTINGS_DESCRIPTION = "This page allows you to configure the global settings.";

    // Values not stored in the database, but keys must be registered
    public static final String KEY_ENCRYPTION_PWD = "settings.encryption.password";
    public static final String KEY_ENCRYPTION_GLOBAL_SALT = "settings.encryption.global.salt";

    public static final String FIELD_ERROR_ENCRYPTION_FIELD_TOO_SHORT = "The value must be at least 8 characters.";
    public static final String FIELD_ERROR_ENCRYPTION_PWD = "Encryption password missing";
    public static final String FIELD_ERROR_ENCRYPTION_GLOBAL_SALT = "Encryption global salt missing";

    private final SettingsGlobalConfigurationFieldModelValidator settingsGlobalConfigurationValidator;

    @Autowired
    public SettingsDescriptor(SettingsDescriptorKey settingsDescriptorKey, SettingsGlobalConfigurationFieldModelValidator settingsGlobalConfigurationValidator) {
        super(settingsDescriptorKey);
        this.settingsGlobalConfigurationValidator = settingsGlobalConfigurationValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(settingsGlobalConfigurationValidator);
    }

}

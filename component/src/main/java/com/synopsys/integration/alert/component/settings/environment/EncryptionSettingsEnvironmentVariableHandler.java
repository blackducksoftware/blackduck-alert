/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.environment;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.component.settings.encryption.model.SettingsEncryptionModel;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

@Component
public class EncryptionSettingsEnvironmentVariableHandler extends EnvironmentVariableHandler<SettingsEncryptionModel> {
    public static final String HANDLER_NAME = "Encryption Settings";
    public static final String ENCRYPTION_PASSWORD_KEY = "ALERT_COMPONENT_SETTINGS_SETTINGS_ENCRYPTION_PASSWORD";
    public static final String ENCRYPTION_SALT_KEY = "ALERT_COMPONENT_SETTINGS_SETTINGS_ENCRYPTION_GLOBAL_SALT";

    private final EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public EncryptionSettingsEnvironmentVariableHandler(EnvironmentVariableUtility environmentVariableUtility) {
        super(HANDLER_NAME, Set.of(ENCRYPTION_PASSWORD_KEY, ENCRYPTION_SALT_KEY));
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    protected Boolean configurationMissingCheck() {
        // we always pull the values from the environment variables for encryption.
        return true;
    }

    @Override
    protected SettingsEncryptionModel configureModel() {
        return new SettingsEncryptionModel();
    }

    @Override
    protected ValidationResponseModel validateConfiguration(SettingsEncryptionModel obfuscatedConfigModel) {
        // Since the encryption utility gets the environment variables, there is nothing to validate here.
        return ValidationResponseModel.success();
    }

    @Override
    protected EnvironmentProcessingResult buildProcessingResult(SettingsEncryptionModel configModel) {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(ENCRYPTION_PASSWORD_KEY, ENCRYPTION_SALT_KEY);

        Optional<String> password = environmentVariableUtility.getEnvironmentValue(ENCRYPTION_PASSWORD_KEY);
        Optional<String> salt = environmentVariableUtility.getEnvironmentValue(ENCRYPTION_SALT_KEY);

        // The encryption utility will read the environment variables.  We just want to be able to log if the variables are set.
        password.ifPresent(unusedValue -> builder.addVariableValue(ENCRYPTION_PASSWORD_KEY, AlertConstants.MASKED_VALUE));
        salt.ifPresent(unusedValue -> builder.addVariableValue(ENCRYPTION_SALT_KEY, AlertConstants.MASKED_VALUE));

        return builder.build();
    }

    @Override
    protected void saveConfiguration(SettingsEncryptionModel configModel, EnvironmentProcessingResult processingResult) {
        // Encryption configuration does not need to be saved to a database.
    }
}

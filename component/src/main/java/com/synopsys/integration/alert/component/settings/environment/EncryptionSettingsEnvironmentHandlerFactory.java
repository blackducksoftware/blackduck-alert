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
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

@Component
public class EncryptionSettingsEnvironmentHandlerFactory implements EnvironmentVariableHandlerFactory {
    public static final String HANDLER_NAME = "Encryption Settings";
    public static final String ENCRYPTION_PASSWORD_KEY = "ALERT_COMPONENT_SETTINGS_SETTINGS_ENCRYPTION_PASSWORD";
    public static final String ENCRYPTION_SALT_KEY = "ALERT_COMPONENT_SETTINGS_SETTINGS_ENCRYPTION_GLOBAL_SALT";

    private final EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public EncryptionSettingsEnvironmentHandlerFactory(EnvironmentVariableUtility environmentVariableUtility) {
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    public EnvironmentVariableHandler build() {
        return new EnvironmentVariableHandler(HANDLER_NAME, Set.of(ENCRYPTION_PASSWORD_KEY, ENCRYPTION_SALT_KEY), this::isConfigurationMissing, this::updateFunction);
    }

    private Boolean isConfigurationMissing() {
        // we always pull the values from the environment variables for encryption.
        return true;
    }

    private EnvironmentProcessingResult updateFunction() {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(ENCRYPTION_PASSWORD_KEY, ENCRYPTION_SALT_KEY);
        Optional<String> password = environmentVariableUtility.getEnvironmentValue(ENCRYPTION_PASSWORD_KEY);
        Optional<String> salt = environmentVariableUtility.getEnvironmentValue(ENCRYPTION_SALT_KEY);

        // The encryption utility will read the environment variables.  We just want to be able to log if the variables are set.
        if (password.isPresent()) {
            builder.addVariableValue(ENCRYPTION_PASSWORD_KEY, AlertConstants.MASKED_VALUE);
        }

        if (salt.isPresent()) {
            builder.addVariableValue(ENCRYPTION_SALT_KEY, AlertConstants.MASKED_VALUE);
        }

        return builder.build();
    }
}

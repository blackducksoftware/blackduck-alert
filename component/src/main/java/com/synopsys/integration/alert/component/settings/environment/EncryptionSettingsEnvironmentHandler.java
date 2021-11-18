/*
 * component
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.settings.environment;

import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

// Encryption settings need to be handled first.  This is to log the encryption environment variables.
@Component
@Order(1)
public class EncryptionSettingsEnvironmentHandler implements EnvironmentVariableHandler {
    public static final String HANDLER_NAME = "Encryption Settings";
    public static final String ENCRYPTION_PASSWORD_KEY = "ALERT_COMPONENT_SETTINGS_SETTINGS_ENCRYPTION_PASSWORD";
    public static final String ENCRYPTION_SALT_KEY = "ALERT_COMPONENT_SETTINGS_SETTINGS_ENCRYPTION_GLOBAL_SALT";

    private final EnvironmentVariableUtility environmentVariableUtility;

    @Autowired
    public EncryptionSettingsEnvironmentHandler(EnvironmentVariableUtility environmentVariableUtility) {
        this.environmentVariableUtility = environmentVariableUtility;
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public Set<String> getVariableNames() {
        return Set.of(ENCRYPTION_PASSWORD_KEY, ENCRYPTION_SALT_KEY);
    }

    @Override
    public Properties updateFromEnvironment() {
        Properties properties = new Properties();
        Optional<String> password = environmentVariableUtility.getEnvironmentValue(ENCRYPTION_PASSWORD_KEY);
        Optional<String> salt = environmentVariableUtility.getEnvironmentValue(ENCRYPTION_SALT_KEY);

        // The encryption utility will read the environment variables.  We just want to be able to log if the variables are set.
        if (password.isPresent()) {
            properties.put(ENCRYPTION_PASSWORD_KEY, AlertRestConstants.MASKED_VALUE);
        }

        if (salt.isPresent()) {
            properties.put(ENCRYPTION_SALT_KEY, AlertRestConstants.MASKED_VALUE);
        }
        return properties;
    }
}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.api.environment.EnvironmentProcessingResult;
import com.blackduck.integration.alert.api.environment.EnvironmentVariableUtility;
import com.blackduck.integration.alert.test.common.EnvironmentVariableMockingUtil;
import com.blackduck.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class EncryptionSettingsEnvironmentVariableHandlerTestIT {
    private static final String TEST_ENCRYPTION_PASSWORD = "testEncryptionPassword";
    private static final String TEST_ENCRYPTION_GLOBAL_SALT = "testGlobalSalt";

    @Test
    void testCleanEnvironment() {
        Environment environment = setupMockedEnvironment();
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EncryptionSettingsEnvironmentVariableHandler encryptionSettingsEnvironmentVariableHandler = new EncryptionSettingsEnvironmentVariableHandler(environmentVariableUtility);
        EnvironmentProcessingResult result = encryptionSettingsEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(EncryptionSettingsEnvironmentVariableHandler.HANDLER_NAME, encryptionSettingsEnvironmentVariableHandler.getName());
        assertTrue(result.hasValues());

        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_PASSWORD_KEY).orElse("Encryption password missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_SALT_KEY).orElse("Encryption global salt missing"));
    }

    private Environment setupMockedEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Set<String> encryptionEnvironmentSettings = Set.of(
            EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_PASSWORD_KEY,
            EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_SALT_KEY
        );
        Predicate<String> hasEnvVarCheck = (variableName) -> !encryptionEnvironmentSettings.contains(variableName);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_PASSWORD_KEY, TEST_ENCRYPTION_PASSWORD);
        EnvironmentVariableMockingUtil.addEnvironmentVariableValueToMock(environment, hasEnvVarCheck, EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_SALT_KEY, TEST_ENCRYPTION_GLOBAL_SALT);
        return environment;
    }
}

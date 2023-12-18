package com.synopsys.integration.alert.component.settings.environment;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.api.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.api.environment.EnvironmentVariableUtility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptionSettingsEnvironmentVariableHandlerTest {
    @Test
    void testEncryptionSetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Set<String> variableNames = Set.of(EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_PASSWORD_KEY, EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_SALT_KEY);
        for (String variableName : variableNames) {
            Mockito.when(environment.containsProperty(variableName)).thenReturn(Boolean.TRUE);
            Mockito.when(environment.getProperty(variableName)).thenReturn("a value");
        }
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EncryptionSettingsEnvironmentVariableHandler encryptionSettingsEnvironmentVariableHandler = new EncryptionSettingsEnvironmentVariableHandler(environmentVariableUtility);
        EnvironmentProcessingResult result = encryptionSettingsEnvironmentVariableHandler.updateFromEnvironment();
        assertTrue(result.hasValues());
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_PASSWORD_KEY).orElse("Password value is missing"));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_SALT_KEY).orElse("Salt value is missing"));
    }

    @Test
    void testEncryptionMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        Set<String> expectedVariableNames = Set.of(
            EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_PASSWORD_KEY,
            EncryptionSettingsEnvironmentVariableHandler.ENCRYPTION_SALT_KEY
        );
        EncryptionSettingsEnvironmentVariableHandler encryptionSettingsEnvironmentVariableHandler = new EncryptionSettingsEnvironmentVariableHandler(environmentVariableUtility);
        EnvironmentProcessingResult result = encryptionSettingsEnvironmentVariableHandler.updateFromEnvironment();
        assertEquals(EncryptionSettingsEnvironmentVariableHandler.HANDLER_NAME, encryptionSettingsEnvironmentVariableHandler.getName());
        assertEquals(expectedVariableNames, encryptionSettingsEnvironmentVariableHandler.getVariableNames());
        assertFalse(result.hasValues());
    }
}

package com.synopsys.integration.alert.component.settings.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.environment.EnvironmentProcessingResult;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

class EncryptionSettingsEnvironmentHandlerFactoryTest {

    @Test
    void testEncryptionSetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Set<String> variableNames = Set.of(EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_PASSWORD_KEY, EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_SALT_KEY);
        for (String variableName : variableNames) {
            Mockito.when(environment.containsProperty(variableName)).thenReturn(Boolean.TRUE);
            Mockito.when(environment.getProperty(variableName)).thenReturn("a value");
        }
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new EncryptionSettingsEnvironmentHandlerFactory(environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertTrue(result.hasValues());
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_PASSWORD_KEY).orElse(null));
        assertEquals(AlertConstants.MASKED_VALUE, result.getVariableValue(EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_SALT_KEY).orElse(null));
    }

    @Test
    void testEncryptionMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        Set<String> expectedVariableNames = Set.of(
            EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_PASSWORD_KEY,
            EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_SALT_KEY);
        EnvironmentVariableHandlerFactory factory = new EncryptionSettingsEnvironmentHandlerFactory(environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertEquals(EncryptionSettingsEnvironmentHandlerFactory.HANDLER_NAME, handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertFalse(result.hasValues());
    }
}

package com.synopsys.integration.alert.component.settings.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;
import com.synopys.integration.alert.test.common.environment.TestEnvironmentHandlerWrapper;

public class EncryptionSettingsEnvironmentHandlerTest {

    @Test
    public void testEncryptionSetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Set<String> variableNames = Set.of(EncryptionSettingsEnvironmentHandler.ENCRYPTION_PASSWORD_KEY, EncryptionSettingsEnvironmentHandler.ENCRYPTION_SALT_KEY);
        for (String variableName : variableNames) {
            Mockito.when(environment.containsProperty(Mockito.eq(variableName))).thenReturn(Boolean.TRUE);
            Mockito.when(environment.getProperty(Mockito.eq(variableName))).thenReturn("a value");
        }
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        TestEnvironmentHandlerWrapper handler = new TestEnvironmentHandlerWrapper(new EncryptionSettingsEnvironmentHandler(environmentVariableUtility));
        handler.updateFromEnvironment();
        assertTrue(handler.hasUpdateOccurred());
        Properties updatedProperties = handler.getUpdatedProperties().orElseThrow(() -> new AssertionError("Properties should exist"));
        assertFalse(updatedProperties.isEmpty());
        assertEquals(AlertRestConstants.MASKED_VALUE, updatedProperties.getProperty(EncryptionSettingsEnvironmentHandler.ENCRYPTION_PASSWORD_KEY));
        assertEquals(AlertRestConstants.MASKED_VALUE, updatedProperties.getProperty(EncryptionSettingsEnvironmentHandler.ENCRYPTION_SALT_KEY));
    }

    @Test
    public void testEncryptionMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        TestEnvironmentHandlerWrapper handler = new TestEnvironmentHandlerWrapper(new EncryptionSettingsEnvironmentHandler(environmentVariableUtility));
        handler.updateFromEnvironment();
        assertFalse(handler.hasUpdateOccurred());
        assertTrue(handler.getUpdatedProperties().stream()
            .allMatch(Properties::isEmpty));
    }
}

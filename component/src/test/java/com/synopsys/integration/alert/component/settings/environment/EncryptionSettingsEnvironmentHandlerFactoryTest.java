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
import com.synopsys.integration.alert.environment.EnvironmentVariableHandler;
import com.synopsys.integration.alert.environment.EnvironmentVariableHandlerFactory;
import com.synopsys.integration.alert.environment.EnvironmentVariableUtility;

public class EncryptionSettingsEnvironmentHandlerFactoryTest {

    @Test
    public void testEncryptionSetInEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        Set<String> variableNames = Set.of(EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_PASSWORD_KEY, EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_SALT_KEY);
        for (String variableName : variableNames) {
            Mockito.when(environment.containsProperty(Mockito.eq(variableName))).thenReturn(Boolean.TRUE);
            Mockito.when(environment.getProperty(Mockito.eq(variableName))).thenReturn("a value");
        }
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        EnvironmentVariableHandlerFactory factory = new EncryptionSettingsEnvironmentHandlerFactory(environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertFalse(updatedProperties.isEmpty());
        assertEquals(AlertRestConstants.MASKED_VALUE, updatedProperties.getProperty(EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_PASSWORD_KEY));
        assertEquals(AlertRestConstants.MASKED_VALUE, updatedProperties.getProperty(EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_SALT_KEY));
    }

    @Test
    public void testEncryptionMissingFromEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        Set<String> expectedVariableNames = Set.of(
            EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_PASSWORD_KEY,
            EncryptionSettingsEnvironmentHandlerFactory.ENCRYPTION_SALT_KEY);
        EnvironmentVariableHandlerFactory factory = new EncryptionSettingsEnvironmentHandlerFactory(environmentVariableUtility);
        EnvironmentVariableHandler handler = factory.build();
        Properties updatedProperties = handler.updateFromEnvironment();
        assertEquals(EncryptionSettingsEnvironmentHandlerFactory.HANDLER_NAME, handler.getName());
        assertEquals(expectedVariableNames, handler.getVariableNames());
        assertTrue(updatedProperties.isEmpty());
    }
}

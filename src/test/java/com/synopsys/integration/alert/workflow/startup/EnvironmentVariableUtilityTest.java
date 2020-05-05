package com.synopsys.integration.alert.workflow.startup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptor;
import com.synopsys.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.synopsys.integration.alert.workflow.startup.component.EnvironmentVariableUtility;

public class EnvironmentVariableUtilityTest {
    private static final String WORKING_PROPERTY_KEY = "ALERT_COMPONENT_SETTINGS_SETTINGS_ENCRYPTION_GLOBAL_SALT";

    @Test
    public void testKeyConversion() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        SettingsDescriptorKey settingsDescriptorKey = new SettingsDescriptorKey();
        String actualPropertyKey = environmentVariableUtility.convertKeyToProperty(settingsDescriptorKey, SettingsDescriptor.KEY_ENCRYPTION_GLOBAL_SALT);
        String expectedPropertyKey = WORKING_PROPERTY_KEY;
        assertEquals(expectedPropertyKey, actualPropertyKey);
    }

    @Test
    public void testHasEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        String workingPropertyKey = WORKING_PROPERTY_KEY;
        Mockito.when(environment.containsProperty(Mockito.eq(workingPropertyKey))).thenReturn(Boolean.TRUE);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        assertFalse(environmentVariableUtility.hasEnvironmentValue("BAD_KEY"));
        assertTrue(environmentVariableUtility.hasEnvironmentValue(workingPropertyKey));
    }

    @Test
    public void testGetEnvironmentValueEmpty() {
        Environment environment = Mockito.mock(Environment.class);
        String workingPropertyKey = WORKING_PROPERTY_KEY;
        Mockito.when(environment.containsProperty(Mockito.eq(workingPropertyKey))).thenReturn(Boolean.TRUE);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        assertTrue(environmentVariableUtility.getEnvironmentValue(workingPropertyKey).isEmpty());
    }

    @Test
    public void testGetEnvironmentValue() {
        Environment environment = Mockito.mock(Environment.class);
        String workingPropertyKey = WORKING_PROPERTY_KEY;
        String expectedValue = "expected value";
        Mockito.when(environment.getProperty(Mockito.eq(workingPropertyKey))).thenReturn(expectedValue);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        Optional<String> optionalValue = environmentVariableUtility.getEnvironmentValue(workingPropertyKey);
        assertTrue(optionalValue.isPresent());
        assertEquals(expectedValue, optionalValue.get());
    }

    @Test
    public void testGetEnvironmentValueDefaultValue() {
        Environment environment = Mockito.mock(Environment.class);
        String workingPropertyKey = WORKING_PROPERTY_KEY;
        String expectedValue = "expected value";
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        Optional<String> optionalValue = environmentVariableUtility.getEnvironmentValue(workingPropertyKey, expectedValue);
        assertTrue(optionalValue.isPresent());
        assertEquals(expectedValue, optionalValue.get());
    }

}

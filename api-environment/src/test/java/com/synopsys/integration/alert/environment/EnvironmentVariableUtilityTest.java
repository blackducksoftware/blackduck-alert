package com.synopsys.integration.alert.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class EnvironmentVariableUtilityTest {
    private static final String WORKING_PROPERTY_KEY = "ALERT_DESCRIPTORTYPE_NAME_SPECIFIC_PROPERTY_KEY";

    private static class TestDescriptorKey extends DescriptorKey {
        public TestDescriptorKey(String universalKey, String displayName) {
            super(universalKey, displayName);
        }
    }

    private static final TestDescriptorKey DESCRIPTOR_KEY = new TestDescriptorKey("descriptortype_name", "Test Descriptor");

    @Test
    public void testKeyConversion() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        String actualPropertyKey = environmentVariableUtility.convertKeyToProperty(DESCRIPTOR_KEY, "specific.property.key");
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

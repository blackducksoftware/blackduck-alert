package com.synopsys.integration.alert.api.environment;

import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvironmentVariableUtilityTest {
    private static final String WORKING_PROPERTY_KEY = "ALERT_DESCRIPTORTYPE_NAME_SPECIFIC_PROPERTY_KEY";

    private static class TestDescriptorKey extends DescriptorKey {
        private static final long serialVersionUID = 577451208866602851L;

        public TestDescriptorKey(String universalKey, String displayName) {
            super(universalKey, displayName);
        }
    }

    private static final TestDescriptorKey DESCRIPTOR_KEY = new TestDescriptorKey("descriptortype_name", "Test Descriptor");

    @Test
    void testKeyConversion() {
        Environment environment = Mockito.mock(Environment.class);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        String actualPropertyKey = environmentVariableUtility.convertKeyToProperty(DESCRIPTOR_KEY, "specific.property.key");
        assertEquals(WORKING_PROPERTY_KEY, actualPropertyKey);
    }

    @Test
    void testHasEnvironment() {
        Environment environment = Mockito.mock(Environment.class);
        String workingPropertyKey = WORKING_PROPERTY_KEY;
        Mockito.when(environment.containsProperty(workingPropertyKey)).thenReturn(Boolean.TRUE);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        assertFalse(environmentVariableUtility.hasEnvironmentValue("BAD_KEY"));
        assertTrue(environmentVariableUtility.hasEnvironmentValue(workingPropertyKey));
    }

    @Test
    void testGetEnvironmentValueEmpty() {
        Environment environment = Mockito.mock(Environment.class);
        String workingPropertyKey = WORKING_PROPERTY_KEY;
        Mockito.when(environment.containsProperty(workingPropertyKey)).thenReturn(Boolean.TRUE);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        assertTrue(environmentVariableUtility.getEnvironmentValue(workingPropertyKey).isEmpty());
    }

    @Test
    void testGetEnvironmentValue() {
        Environment environment = Mockito.mock(Environment.class);
        String workingPropertyKey = WORKING_PROPERTY_KEY;
        String expectedValue = "expected value";
        Mockito.when(environment.getProperty(workingPropertyKey)).thenReturn(expectedValue);
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        Optional<String> optionalValue = environmentVariableUtility.getEnvironmentValue(workingPropertyKey);
        assertTrue(optionalValue.isPresent());
        assertEquals(expectedValue, optionalValue.get());
    }

    @Test
    void testGetEnvironmentValueDefaultValue() {
        Environment environment = Mockito.mock(Environment.class);
        String expectedValue = "expected value";
        EnvironmentVariableUtility environmentVariableUtility = new EnvironmentVariableUtility(environment);
        Optional<String> optionalValue = environmentVariableUtility.getEnvironmentValue(WORKING_PROPERTY_KEY, expectedValue);
        assertTrue(optionalValue.isPresent());
        assertEquals(expectedValue, optionalValue.get());
    }
}

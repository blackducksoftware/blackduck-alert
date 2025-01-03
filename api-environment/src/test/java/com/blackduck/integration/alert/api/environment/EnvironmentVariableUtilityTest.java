/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

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

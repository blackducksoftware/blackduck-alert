package com.synopsys.integration.alert.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

public class EnvironmentVariableHandlerTest {

    @Test
    public void testHandlerConfigExists() {
        String name = "name";
        Set<String> variableNames = Set.of("VARIABLE_1, VARIABLE_2", "VARIABLE_3");

        EnvironmentVariableHandler handler = new EnvironmentVariableHandler(name, variableNames, () -> Boolean.FALSE, Properties::new);
        Properties updatedProperties = handler.updateFromEnvironment();
        assertFalse(handler.isConfigurationMissing());
        assertTrue(updatedProperties.isEmpty());
        assertEquals(variableNames, handler.getVariableNames());
        assertEquals(name, handler.getName());
    }

    @Test
    public void testHandlerNoUpdates() {
        String name = "name";
        Set<String> variableNames = Set.of("VARIABLE_1, VARIABLE_2", "VARIABLE_3");

        EnvironmentVariableHandler handler = new EnvironmentVariableHandler(name, variableNames, () -> Boolean.TRUE, Properties::new);
        Properties updatedProperties = handler.updateFromEnvironment();
        assertTrue(handler.isConfigurationMissing());
        assertTrue(updatedProperties.isEmpty());
        assertEquals(variableNames, handler.getVariableNames());
        assertEquals(name, handler.getName());
    }

    @Test
    public void testHandlerConfigUpdated() {
        String name = "name";
        Set<String> variableNames = Set.of("VARIABLE_1, VARIABLE_2", "VARIABLE_3");
        Supplier<Properties> updateFunction = () -> {
            Properties properties = new Properties();
            properties.put("VARIABLE_1", "variable_1_value");
            properties.put("VARIABLE_3", "variable_3_value");
            return properties;
        };

        EnvironmentVariableHandler handler = new EnvironmentVariableHandler(name, variableNames, () -> Boolean.TRUE, updateFunction);
        Properties updatedProperties = handler.updateFromEnvironment();
        assertTrue(handler.isConfigurationMissing());
        assertEquals(variableNames, handler.getVariableNames());
        assertEquals(name, handler.getName());
        assertEquals("variable_1_value", updatedProperties.getProperty("VARIABLE_1"));
        assertNull(updatedProperties.getProperty("VARIABLE_2"));
        assertEquals("variable_3_value", updatedProperties.getProperty("VARIABLE_3"));
    }
}

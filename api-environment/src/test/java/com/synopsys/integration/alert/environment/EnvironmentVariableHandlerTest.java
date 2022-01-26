package com.synopsys.integration.alert.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class EnvironmentVariableHandlerTest {

    @Test
    void testHandlerConfigExists() {
        String name = "name";
        Set<String> variableNames = Set.of("VARIABLE_1, VARIABLE_2", "VARIABLE_3");

        EnvironmentVariableHandler handler = new EnvironmentVariableHandler(name, variableNames, () -> Boolean.FALSE, EnvironmentProcessingResult::empty);
        EnvironmentProcessingResult updatedProperties = handler.updateFromEnvironment();
        assertFalse(handler.isConfigurationMissing());
        assertFalse(updatedProperties.hasValues());
        assertEquals(variableNames, handler.getVariableNames());
        assertEquals(name, handler.getName());
    }

    @Test
    void testHandlerNoUpdates() {
        String name = "name";
        Set<String> variableNames = Set.of("VARIABLE_1, VARIABLE_2", "VARIABLE_3");

        EnvironmentVariableHandler handler = new EnvironmentVariableHandler(name, variableNames, () -> Boolean.TRUE, EnvironmentProcessingResult::empty);
        EnvironmentProcessingResult updatedProperties = handler.updateFromEnvironment();
        assertTrue(handler.isConfigurationMissing());
        assertFalse(updatedProperties.hasValues());
        assertEquals(variableNames, handler.getVariableNames());
        assertEquals(name, handler.getName());
    }

    @Test
    void testHandlerConfigUpdated() {
        String name = "name";
        Set<String> variableNames = Set.of("VARIABLE_1, VARIABLE_2", "VARIABLE_3");
        Supplier<EnvironmentProcessingResult> updateFunction = () -> {
            EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
            builder.addVariableValue("VARIABLE_1", "variable_1_value")
                .addVariableValue("VARIABLE_3", "variable_3_value");
            return builder.build();
        };

        EnvironmentVariableHandler handler = new EnvironmentVariableHandler(name, variableNames, () -> Boolean.TRUE, updateFunction);
        EnvironmentProcessingResult result = handler.updateFromEnvironment();
        assertTrue(handler.isConfigurationMissing());
        assertEquals(variableNames, handler.getVariableNames());
        assertEquals(name, handler.getName());
        assertEquals("variable_1_value", result.getVariableValue("VARIABLE_1").orElse(null));
        assertTrue(result.getVariableValue("VARIABLE_2").isEmpty());
        assertEquals("variable_3_value", result.getVariableValue("VARIABLE_3").orElse(null));
    }
}

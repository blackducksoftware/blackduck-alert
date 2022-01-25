package com.synopsys.integration.alert.environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.AlertConstants;

class EnvironmentProcessingResultTest {

    @Test
    void testEmptyBuilder() {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        EnvironmentProcessingResult result = builder.build();
        Optional<String> actualValue = result.getVariableValue("A_ENV_VAR");
        assertTrue(result.isEmpty());
        assertTrue(result.getVariableNames().isEmpty());
        assertTrue(result.getVariableValues().isEmpty());
        assertTrue(actualValue.isEmpty());
    }

    @Test
    void testAddVariableValue() {
        String variableName = "A_ENV_VAR";
        String value = "value";
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        builder.addVariableValue(variableName, value);
        EnvironmentProcessingResult result = builder.build();
        String actualValue = result.getVariableValue(variableName).orElseThrow(() -> new IllegalStateException("The variable name specified should be present in the result."));
        assertEquals(1, result.getVariableNames().size());
        assertEquals(1, result.getVariableValues().size());
        assertEquals(value, actualValue);
    }

    @Test
    void testAddVariableValueWithSensitiveParamFalse() {
        String variableName = "A_ENV_VAR";
        String value = "value";
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        builder.addVariableValue(variableName, value, false);
        EnvironmentProcessingResult result = builder.build();
        String actualValue = result.getVariableValue(variableName).orElseThrow(() -> new IllegalStateException("The variable name specified should be present in the result."));
        assertEquals(1, result.getVariableNames().size());
        assertEquals(1, result.getVariableValues().size());
        assertEquals(value, actualValue);
    }

    @Test
    void testAddVariableValueWithSensitiveParamTrue() {
        String variableName = "A_ENV_VAR";
        String value = "value";
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        builder.addVariableValue(variableName, value, true);
        EnvironmentProcessingResult result = builder.build();
        String actualValue = result.getVariableValue(variableName).orElseThrow(() -> new IllegalStateException("The variable name specified should be present in the result."));
        assertEquals(1, result.getVariableNames().size());
        assertEquals(1, result.getVariableValues().size());
        assertEquals(AlertConstants.MASKED_VALUE, actualValue);
    }

    @Test
    void testAddSensitiveValue() {
        String variableName = "A_ENV_VAR";
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        builder.addSensitiveVariable(variableName);
        EnvironmentProcessingResult result = builder.build();
        String actualValue = result.getVariableValue(variableName).orElseThrow(() -> new IllegalStateException("The variable name specified should be present in the result."));
        assertEquals(1, result.getVariableNames().size());
        assertEquals(1, result.getVariableValues().size());
        assertEquals(AlertConstants.MASKED_VALUE, actualValue);
    }
}

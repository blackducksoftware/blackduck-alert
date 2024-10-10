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

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class EnvironmentProcessingResultTest {

    @Test
    void testEmptyMethodConstructor() {
        EnvironmentProcessingResult result = EnvironmentProcessingResult.empty();
        Optional<String> actualValue = result.getVariableValue("A_ENV_VAR");
        assertFalse(result.hasValues());
        assertTrue(result.getVariableNames().isEmpty());
        assertTrue(actualValue.isEmpty());
    }

    @Test
    void testDefaultConstructor() {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        EnvironmentProcessingResult result = builder.build();
        Optional<String> actualValue = result.getVariableValue("A_ENV_VAR");
        assertFalse(result.hasValues());
        assertTrue(result.getVariableNames().isEmpty());
        assertTrue(actualValue.isEmpty());
    }

    @Test
    void testArrayConstructor() {
        String environmentVariable1 = "A_ENV_VAR_1";
        String environmentVariable2 = "A_ENV_VAR_2";
        List<String> variableNames = List.of(environmentVariable1, environmentVariable2);
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(environmentVariable1, environmentVariable2);
        EnvironmentProcessingResult result = builder.build();
        assertFalse(result.hasValues());
        assertTrue(result.getVariableNames().containsAll(variableNames));
    }

    @Test
    void testListConstructor() {
        List<String> variableNames = List.of("A_ENV_VAR_1", "A_ENV_VAR_2");
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(variableNames);
        EnvironmentProcessingResult result = builder.build();
        assertFalse(result.hasValues());
        assertTrue(result.getVariableNames().containsAll(variableNames));
    }

    @Test
    void testAddValueAddsVariableName() {
        String variableName = "A_ENV_VAR";
        String value = "value";
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        builder.addVariableValue(variableName, value);
        EnvironmentProcessingResult result = builder.build();
        String actualValue = result.getVariableValue(variableName).orElseThrow(() -> new IllegalStateException("The variable name specified should be present in the result."));
        assertEquals(1, result.getVariableNames().size());
        assertEquals(value, actualValue);
    }

    @Test
    void testAddDuplicateVariableName() {
        String variableName = "A_ENV_VAR";
        String value = "value";
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder(variableName);
        builder.addVariableNames(List.of(variableName));
        builder.addVariableValue(variableName, value);
        EnvironmentProcessingResult result = builder.build();
        String actualValue = result.getVariableValue(variableName).orElseThrow(() -> new IllegalStateException("The variable name specified should be present in the result."));
        assertEquals(1, result.getVariableNames().size());
        assertEquals(value, actualValue);
    }

    @Test
    void testEmptyBuilder() {
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        EnvironmentProcessingResult result = builder.build();
        Optional<String> actualValue = result.getVariableValue("A_ENV_VAR");
        assertFalse(result.hasValues());
        assertTrue(result.getVariableNames().isEmpty());
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
        assertEquals(value, actualValue);
    }

    @Test
    void testAddVariableNullValue() {
        String variableName = "A_ENV_VAR";
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        builder.addVariableValue(variableName, null);
        EnvironmentProcessingResult result = builder.build();
        String actualValue = result.getVariableValue(variableName).orElseThrow(() -> new IllegalStateException("The variable name specified should be present in the result."));
        assertEquals(1, result.getVariableNames().size());
        assertEquals(StringUtils.EMPTY, actualValue);
    }

    @Test
    void testAddVariableWhitespaceValue() {
        String variableName = "A_ENV_VAR";
        EnvironmentProcessingResult.Builder builder = new EnvironmentProcessingResult.Builder();
        builder.addVariableValue(variableName, "      \n\t\r      ");
        EnvironmentProcessingResult result = builder.build();
        String actualValue = result.getVariableValue(variableName).orElseThrow(() -> new IllegalStateException("The variable name specified should be present in the result."));
        assertEquals(1, result.getVariableNames().size());
        assertEquals(StringUtils.EMPTY, actualValue);
    }
}

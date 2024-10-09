/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.environment;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.util.Stringable;

public class EnvironmentProcessingResult extends Stringable {
    private final Set<String> variableNames;
    private final Map<String, String> variableValues;

    public static EnvironmentProcessingResult empty() {
        return new EnvironmentProcessingResult(Set.of(), Map.of());
    }

    private EnvironmentProcessingResult(Set<String> variableNames, Map<String, String> variableValues) {
        this.variableNames = variableNames;
        this.variableValues = variableValues;
    }

    public boolean hasValues() {
        return !variableValues.isEmpty();
    }

    public Set<String> getVariableNames() {
        return variableNames;
    }

    public Optional<String> getVariableValue(String variableName) {
        return Optional.ofNullable(variableValues.get(variableName));
    }

    public static class Builder {
        private final Set<String> variableNames = new HashSet<>();
        private final Map<String, String> variableValues = new HashMap<>();

        public Builder(String... variableNames) {
            this.addVariableNames(Arrays.asList(variableNames));
        }

        public Builder(Collection<String> variableNames) {
            this.addVariableNames(variableNames);
        }

        public EnvironmentProcessingResult build() {
            return new EnvironmentProcessingResult(this.variableNames, this.variableValues);
        }

        public Builder addVariableNames(Collection<String> variableNames) {
            this.variableNames.addAll(variableNames);
            return this;
        }

        public Builder addVariableValue(String variableName, @Nullable String value) {
            this.variableNames.add(variableName);
            String propertyValue = StringUtils.trimToEmpty(value);
            this.variableValues.put(variableName, propertyValue);

            return this;
        }
    }
}

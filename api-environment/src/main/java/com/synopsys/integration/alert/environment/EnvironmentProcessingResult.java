/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.environment;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.util.Stringable;

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
        return Optional.ofNullable(variableValues.getOrDefault(variableName, null));
    }

    public Map<String, String> getVariableValues() {
        return variableValues;
    }

    public static class Builder {
        private final Set<String> variableNames;
        private final Map<String, String> variableValues;

        private Builder() {
            variableNames = new HashSet<>();
            variableValues = new HashMap<>();
        }

        public Builder(String... variableNames) {
            this();
            this.addVariableNames(variableNames);
        }

        public Builder(Collection<String> variableNames) {
            this();
            this.addVariableNames(variableNames);
        }

        public EnvironmentProcessingResult build() {
            return new EnvironmentProcessingResult(this.variableNames, this.variableValues);
        }

        public Builder addVariableNames(String... variableNames) {
            return addVariableNames(Arrays.asList(variableNames));
        }

        public Builder addVariableNames(Collection<String> variableNames) {
            this.variableNames.addAll(variableNames);
            return this;
        }

        public Builder addSensitiveVariable(String variableName, boolean isVariableSet) {
            return addVariableValue(variableName, AlertConstants.MASKED_VALUE, isVariableSet);
        }

        public Builder addVariableValue(String variableName, @Nullable String value, boolean isVariableSet) {
            this.variableNames.add(variableName);
            if (isVariableSet) {
                String propertyValue = StringUtils.trimToEmpty(value);
                this.variableValues.put(variableName, propertyValue);
            }
            return this;
        }
    }
}

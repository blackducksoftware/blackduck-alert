package com.synopsys.integration.alert.environment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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

    public boolean isEmpty() {
        return variableNames.isEmpty() && variableValues.isEmpty();
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
        private final Set<String> variableNames = new HashSet<>();
        private final Map<String, String> variableValues = new HashMap<>();

        public EnvironmentProcessingResult build() {
            return new EnvironmentProcessingResult(this.variableNames, this.variableValues);
        }

        public Builder addSensitiveVariable(String variableName) {
            return addVariableValue(variableName, null, true);
        }

        public Builder addVariableValue(String variableName, String value) {
            return addVariableValue(variableName, value, false);
        }

        public Builder addVariableValue(String variableName, String value, boolean isSensitive) {
            variableNames.add(variableName);
            String propertyValue = StringUtils.trimToEmpty(value);
            if (isSensitive) {
                propertyValue = AlertConstants.MASKED_VALUE;
            }
            variableValues.put(variableName, propertyValue);
            return this;
        }
    }

}

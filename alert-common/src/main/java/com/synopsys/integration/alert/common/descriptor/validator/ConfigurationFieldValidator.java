/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

public class ConfigurationFieldValidator {
    private final Map<String, FieldValueModel> fieldMap;
    private final Set<AlertFieldStatus> statuses;

    public static ConfigurationFieldValidator fromFieldModel(FieldModel fieldModel) {
        return new ConfigurationFieldValidator(fieldModel.getKeyToValues());
    }

    public static ConfigurationFieldValidator fromJobFieldModel(JobFieldModel jobFieldModel) {
        Map<String, FieldValueModel> mapToValidate = jobFieldModel.getFieldModels()
            .stream()
            .map(FieldModel::getKeyToValues)
            .map(Map::entrySet)
            .flatMap(Set::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (old, newIgnored) -> old)); // Merge operation is equivalent to Map::putIfAbsent --rotte
        return new ConfigurationFieldValidator(mapToValidate);
    }

    public ConfigurationFieldValidator(Map<String, FieldValueModel> mapToValidate) {
        this.fieldMap = mapToValidate;
        statuses = new HashSet<>();
    }

    public Set<AlertFieldStatus> getValidationResults() {
        return statuses;
    }

    public void validateRequiredFieldIsNotBlank(String fieldKey) {
        if (fieldContainsNoData(fieldKey)) {
            statuses.add(AlertFieldStatus.error(fieldKey, "Required field missing"));
        }
    }

    public void validateRequiredFieldsAreNotBlank(List<String> fieldKeys) {
        for (String fieldKey : fieldKeys) {
            validateRequiredFieldIsNotBlank(fieldKey);
        }
    }

    public void validateAllOrNoneSet(String... relatedFieldKeys) {
        boolean anyFieldSet = Arrays.stream(relatedFieldKeys)
                                .anyMatch(this::fieldContainsData);

        if (anyFieldSet) {
            validateRequiredFieldsAreNotBlank(Arrays.asList(relatedFieldKeys));
        }
    }

    public void containsDisallowedRelatedField(String fieldLabel, String disallowedRelatedFieldKey) {
        if (fieldContainsData(disallowedRelatedFieldKey)) {
            statuses.add(AlertFieldStatus.error(disallowedRelatedFieldKey, String.format("Cannot be set if %s is already set", fieldLabel)));
        }
    }

    public void containsDisallowedRelatedFields(String fieldLabel, List<String> fieldKeys) {
        for (String fieldKey : fieldKeys) {
            containsDisallowedRelatedField(fieldLabel, fieldKey);
        }
    }

    public void validateIsANumber(String fieldKey) {
        boolean isANumberOrEmpty = getStringValue(fieldKey)
                                       .map(NumberUtils::isCreatable)
                                       .orElse(true);

        if (!isANumberOrEmpty) {
            statuses.add(AlertFieldStatus.error(fieldKey, NumberConfigField.NOT_AN_INTEGER_VALUE));
        }
    }

    public void validateIsAnOption(String fieldKey, List<String> options) {
        boolean isValueInOptions = getFieldValues(fieldKey)
                                       .map(model -> model.getValues()
                                                         .stream()
                                                         .anyMatch(options::contains))
                                       .orElse(false);
        if (!isValueInOptions) {
            statuses.add(AlertFieldStatus.error(fieldKey, "Invalid option selected"));
        }
    }

    public void addValidationResults(AlertFieldStatus... alertFieldStatuses) {
        statuses.addAll(Arrays.asList(alertFieldStatuses));
    }

    public boolean fieldContainsData(String fieldKey) {
        return !fieldContainsNoData(fieldKey);
    }

    public boolean fieldContainsNoData(String fieldKey) {
        return getFieldValues(fieldKey)
                   .map(FieldValueModel::containsNoData)
                   .orElse(true);
    }

    public boolean fieldHasNoReadableValue(String fieldKey) {
        return !fieldHasReadableValue(fieldKey);
    }

    public boolean fieldHasReadableValue(String fieldKey) {
        return getFieldValues(fieldKey)
                  .map(FieldValueModel::hasValues)
                  .orElse(false);
    }

    public Optional<FieldValueModel> getFieldValues(String fieldKey) {
        return Optional.ofNullable(fieldMap.get(fieldKey));
    }

    public Optional<String> getStringValue(String fieldKey) {
        return getFieldValues(fieldKey).flatMap(FieldValueModel::getValue);
    }

    public Optional<Boolean> getBooleanValue(String fieldKey) {
        return getFieldValues(fieldKey).flatMap(FieldValueModel::getValue).map(Boolean::valueOf);
    }


}

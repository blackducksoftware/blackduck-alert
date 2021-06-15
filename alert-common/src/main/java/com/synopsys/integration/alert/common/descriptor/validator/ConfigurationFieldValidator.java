/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.validator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class ConfigurationFieldValidator {
    private final FieldModel fieldModel;

    public ConfigurationFieldValidator(FieldModel fieldModel) {
        this.fieldModel = fieldModel;
    }

    public Optional<AlertFieldStatus> validateIsARequiredField(String fieldKey) {
        if (fieldContainsData(fieldKey)) {
            return Optional.empty();
        }

        return Optional.of(AlertFieldStatus.error(fieldKey, "Required field missing"));
    }

    public List<AlertFieldStatus> containsRequiredFields(List<String> fieldKeys) {
        return fieldKeys.stream()
                   .map(key -> validateIsARequiredField(key))
                   .flatMap(Optional::stream)
                   .collect(Collectors.toList());
    }

    public List<AlertFieldStatus> containsDisallowedRelatedFields(String fieldLabel, List<String> fieldKeys) {
        return fieldKeys.stream()
                   .filter(key -> fieldContainsData(key))
                   .map(key -> AlertFieldStatus.error(key, String.format("Cannot be set if %s is already set", fieldLabel)))
                   .collect(Collectors.toList());
    }

    public Optional<AlertFieldStatus> validateIsANumber(String fieldKey) {
        boolean isANumberOrEmpty = getFieldValues(fieldKey)
                                       .flatMap(FieldValueModel::getValue)
                                       .map(NumberUtils::isCreatable)
                                       .orElse(true);
        if (isANumberOrEmpty) {
            return Optional.empty();
        }

        return Optional.of(AlertFieldStatus.error(fieldKey, NumberConfigField.NOT_AN_INTEGER_VALUE));
    }

    public Optional<AlertFieldStatus> validateIsAnOption(String fieldKey, List<String> options) {
        boolean isValueInOptions = getFieldValues(fieldKey)
                                       .map(model -> model.getValues()
                                                         .stream()
                                                         .anyMatch(options::contains))
                                       .orElse(false);
        if (isValueInOptions) {
            return Optional.empty();
        }

        return Optional.of(AlertFieldStatus.error(fieldKey, "Invalid option selected"));
    }

    private boolean fieldContainsData(String fieldKey) {
        return !getFieldValues(fieldKey).map(FieldValueModel::containsNoData).orElse(true);
    }

    private Optional<FieldValueModel> getFieldValues(String fieldKey) {
        return fieldModel.getFieldValueModel(fieldKey);
    }

}

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

import com.synopsys.integration.alert.common.descriptor.config.field.NumberConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public final class FieldValidator {

    // TODO remove severity of none and return optional here that only returns value if there's an issue
    public static AlertFieldStatus validateIsARequiredField(FieldModel fieldModel, String fieldKey) {
        if (fieldContainsData(fieldModel, fieldKey)) {
            return AlertFieldStatus.success(fieldKey);
        }

        return AlertFieldStatus.error(fieldKey, "Required field missing");
    }

    public static List<AlertFieldStatus> containsRequiredFields(FieldModel fieldModel, List<String> fieldKeys) {
        return fieldKeys.stream()
                   .map(key -> validateIsARequiredField(fieldModel, key))
                   .collect(Collectors.toList());
    }

    public static List<AlertFieldStatus> containsDisallowedRelatedFields(FieldModel fieldModel, String fieldLabel, List<String> fieldKeys) {
        return fieldKeys.stream()
                   .filter(key -> fieldContainsData(fieldModel, key))
                   .map(key -> AlertFieldStatus.error(key, String.format("Cannot be set if %s is already set", fieldLabel)))
                   .collect(Collectors.toList());
    }

    public static AlertFieldStatus validateIsANumber(FieldModel fieldModel, String fieldKey) {
        Optional<String> fieldValue = getFieldValues(fieldModel, fieldKey).flatMap(FieldValueModel::getValue);
        if (fieldValue.isPresent()) {
            String value = fieldValue.get();
            try {
                Integer.valueOf(value);
            } catch (NumberFormatException ex) {
                return AlertFieldStatus.error(fieldKey, NumberConfigField.NOT_AN_INTEGER_VALUE);
            }
        }

        return AlertFieldStatus.success(fieldKey);
    }

    private static boolean fieldContainsData(FieldModel fieldModel, String fieldKey) {
        return !getFieldValues(fieldModel, fieldKey).map(FieldValueModel::containsNoData).orElse(true);
    }

    private static Optional<FieldValueModel> getFieldValues(FieldModel fieldModel, String fieldKey) {
        return fieldModel.getFieldValueModel(fieldKey);
    }

}

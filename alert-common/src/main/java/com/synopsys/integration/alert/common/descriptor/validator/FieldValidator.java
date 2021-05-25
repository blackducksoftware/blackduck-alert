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

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public final class FieldValidator {

    public static AlertFieldStatus containsRequiredField(FieldModel fieldModel, String fieldKey) {
        Optional<FieldValueModel> fieldValues = getFieldValues(fieldModel, fieldKey);
        if (fieldValues.isPresent()) {
            FieldValueModel fieldValueModel = fieldValues.get();
            if (!fieldValueModel.containsNoData()) {
                return AlertFieldStatus.success(fieldKey);
            }
        }

        return AlertFieldStatus.error(fieldKey, "Required field missing");
    }

    public static List<AlertFieldStatus> containsRelatedRequiredFields(FieldModel fieldModel, List<String> fieldKeys) {
        return fieldKeys.stream()
                   .map(key -> containsRequiredField(fieldModel, key))
                   .collect(Collectors.toList());
    }

    private static Optional<FieldValueModel> getFieldValues(FieldModel fieldModel, String fieldKey) {
        return fieldModel.getFieldValueModel(fieldKey);
    }
    
}

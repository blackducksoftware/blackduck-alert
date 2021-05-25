/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public abstract class GlobalValidator {

    protected abstract Set<AlertFieldStatus> validate(FieldModel fieldModel);

    public List<AlertFieldStatus> validateFieldModel(FieldModel fieldModel) {
        Set<AlertFieldStatus> fieldValidations = validate(fieldModel);
        boolean hasFailedValidation = fieldValidations.stream().anyMatch(validation -> FieldStatusSeverity.NONE != validation.getSeverity());
        if (hasFailedValidation) {
            return fieldValidations.stream()
                       .filter(validation -> FieldStatusSeverity.NONE != validation.getSeverity())
                       .collect(Collectors.toList());
        }
        return List.of();
    }
}

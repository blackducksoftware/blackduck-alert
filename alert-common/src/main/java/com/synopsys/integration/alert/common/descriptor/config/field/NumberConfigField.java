/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class NumberConfigField extends ConfigField {
    public static final String NOT_AN_INTEGER_VALUE = "Not an integer value";

    public NumberConfigField(String key, String label, String description) {
        super(key, label, description, FieldType.NUMBER_INPUT);
        createValidators(List.of(this::validateIsNumber), null);
    }

    private ValidationResult validateIsNumber(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        if (fieldToValidate.hasValues()) {
            String value = fieldToValidate.getValue().orElse("");
            try {
                Integer.valueOf(value);
            } catch (NumberFormatException ex) {
                return ValidationResult.errors(NOT_AN_INTEGER_VALUE);
            }
        }

        return ValidationResult.success();
    }

}

/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class CheckboxConfigField extends ConfigField {
    public CheckboxConfigField(String key, String label, String description) {
        this(key, label, description, FieldType.CHECKBOX_INPUT);
    }

    protected CheckboxConfigField(String key, String label, String description, FieldType fieldType) {
        super(key, label, description, fieldType);
        applyValidationFunctions(this::validateValueIsBoolean);
        applyDefaultValue(Boolean.FALSE.toString());
    }

    private ValidationResult validateValueIsBoolean(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        if (fieldToValidate.hasValues()) {
            String value = fieldToValidate.getValue().orElse("");
            boolean trueTextPresent = Boolean.TRUE.toString().equalsIgnoreCase(value);
            boolean falseTextPresent = Boolean.FALSE.toString().equalsIgnoreCase(value);
            if (!trueTextPresent && !falseTextPresent) {
                ValidationResult.errors("Not a boolean value 'true' or 'false'");
            }
        }
        return ValidationResult.success();
    }

    @Override
    public ConfigField applyDefaultValue(String defaultValue) {
        setDefaultValues(new HashSet<>());
        return super.applyDefaultValue(defaultValue);
    }

    @Override
    public ConfigField applyDefaultValues(Set<String> defaultValues) {
        Optional<String> firstValue = defaultValues.stream().findFirst();
        if (firstValue.isPresent()) {
            applyDefaultValue(firstValue.get());
        }
        return this;
    }
}

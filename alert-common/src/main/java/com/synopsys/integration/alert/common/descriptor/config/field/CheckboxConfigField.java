/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.descriptor.config.field.validators.ValidationResult;
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

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

import java.util.Collection;
import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class CheckboxConfigField extends ConfigField {
    public CheckboxConfigField(String key, String label, String description, boolean required, String panel) {
        super(key, label, description, FieldType.CHECKBOX_INPUT, required, false, panel);
    }

    public CheckboxConfigField(String key, String label, String description, boolean required, String panel, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.CHECKBOX_INPUT, required, false, panel);
        createValidators(List.of(this::validateValueIsBoolean), validationFunctions);
    }

    public CheckboxConfigField(String key, String label, String description, boolean required) {
        super(key, label, description, FieldType.CHECKBOX_INPUT, required, false);
        createValidators(List.of(this::validateValueIsBoolean), null);
    }

    public CheckboxConfigField(String key, String label, String description, boolean required, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.CHECKBOX_INPUT, required, false);
        createValidators(List.of(this::validateValueIsBoolean), validationFunctions);
    }

    protected CheckboxConfigField(String key, String label, String description, FieldType fieldType, boolean required) {
        super(key, label, description, fieldType, required, false);
    }

    public static CheckboxConfigField create(String key, String label, String description) {
        return new CheckboxConfigField(key, label, description, false);
    }

    public static CheckboxConfigField create(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new CheckboxConfigField(key, label, description, false, validationFunctions);
    }

    public static CheckboxConfigField createRequired(String key, String label, String description) {
        return new CheckboxConfigField(key, label, description, true);
    }

    public static CheckboxConfigField createRequired(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new CheckboxConfigField(key, label, description, true, validationFunctions);
    }

    public static CheckboxConfigField createPanel(String key, String label, String description, String panel) {
        return new CheckboxConfigField(key, label, description, false, panel);
    }

    public static CheckboxConfigField createPanel(String key, String label, String description, String panel, ConfigValidationFunction... validationFunctions) {
        return new CheckboxConfigField(key, label, description, false, panel, validationFunctions);
    }

    private Collection<String> validateValueIsBoolean(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        if (fieldToValidate.hasValues()) {
            final String value = fieldToValidate.getValue().orElse("");
            final boolean trueTextPresent = Boolean.TRUE.toString().equalsIgnoreCase(value);
            final boolean falseTextPresent = Boolean.FALSE.toString().equalsIgnoreCase(value);
            if (!trueTextPresent && !falseTextPresent) {
                List.of("Not a boolean value 'true' or 'false'");
            }
        }
        return List.of();
    }
}

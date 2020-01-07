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

public class NumberConfigField extends ConfigField {
    public static final String NOT_AN_INTEGER_VALUE = "Not an integer value";

    public NumberConfigField(String key, String label, String description, boolean required, boolean sensitive, String panel) {
        super(key, label, description, FieldType.NUMBER_INPUT, required, sensitive, panel);
        createValidators(List.of(this::validateIsNumber), null);
    }

    public NumberConfigField(String key, String label, String description, boolean required, boolean sensitive, String panel, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.NUMBER_INPUT, required, sensitive, panel);
        createValidators(List.of(this::validateIsNumber), validationFunctions);
    }

    public NumberConfigField(String key, String label, String description, boolean required, boolean sensitive) {
        super(key, label, description, FieldType.NUMBER_INPUT, required, sensitive);
        createValidators(List.of(this::validateIsNumber), null);
    }

    public NumberConfigField(String key, String label, String description, boolean required, boolean sensitive, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.NUMBER_INPUT, required, sensitive);
        createValidators(List.of(this::validateIsNumber), validationFunctions);
    }

    public static NumberConfigField create(String key, String label, String description) {
        return new NumberConfigField(key, label, description, false, false);
    }

    public static NumberConfigField create(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new NumberConfigField(key, label, description, false, false, validationFunctions);
    }

    public static NumberConfigField createRequired(String key, String label, String description) {
        return new NumberConfigField(key, label, description, true, false);
    }

    public static NumberConfigField createRequired(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new NumberConfigField(key, label, description, true, false, validationFunctions);
    }

    public static NumberConfigField createPanel(String key, String label, String description, String panel) {
        return new NumberConfigField(key, label, description, false, false, panel);
    }

    public static NumberConfigField createPanel(String key, String label, String description, String panel, ConfigValidationFunction... validationFunctions) {
        return new NumberConfigField(key, label, description, false, false, panel, validationFunctions);
    }

    private Collection<String> validateIsNumber(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        if (fieldToValidate.hasValues()) {
            final String value = fieldToValidate.getValue().orElse("");
            try {
                Integer.valueOf(value);
            } catch (final NumberFormatException ex) {
                return List.of(NOT_AN_INTEGER_VALUE);
            }
        }

        return List.of();
    }

}

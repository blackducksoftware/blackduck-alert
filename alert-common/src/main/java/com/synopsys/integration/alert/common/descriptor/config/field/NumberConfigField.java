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

import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class NumberConfigField extends ConfigField {
    public static final String NOT_AN_INTEGER_VALUE = "Not an integer value";

    public NumberConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final String panel) {
        super(key, label, description, FieldType.NUMBER_INPUT.getFieldTypeName(), required, sensitive, panel);
    }

    public NumberConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final String panel, final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.NUMBER_INPUT.getFieldTypeName(), required, sensitive, panel, validationFunction);
    }

    public NumberConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive) {
        super(key, label, description, FieldType.NUMBER_INPUT.getFieldTypeName(), required, sensitive);
    }

    public NumberConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.NUMBER_INPUT.getFieldTypeName(), required, sensitive, validationFunction);
    }

    public static NumberConfigField create(final String key, final String label, final String description) {
        return new NumberConfigField(key, label, description, false, false);
    }

    public static NumberConfigField create(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new NumberConfigField(key, label, description, false, false, validationFunction);
    }

    public static NumberConfigField createRequired(final String key, final String label, final String description) {
        return new NumberConfigField(key, label, description, true, false);
    }

    public static NumberConfigField createRequired(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new NumberConfigField(key, label, description, true, false, validationFunction);
    }

    public static NumberConfigField createPanel(final String key, final String label, final String description, final String panel) {
        return new NumberConfigField(key, label, description, false, false, panel);
    }

    public static NumberConfigField createPanel(final String key, final String label, final String description, final String panel, final ConfigValidationFunction validationFunction) {
        return new NumberConfigField(key, label, description, false, false, panel, validationFunction);
    }

    @Override
    public Collection<String> validate(final FieldValueModel fieldValueModel, final FieldModel fieldModel) {
        final List<ConfigValidationFunction> validationFunctions;
        if (null != getValidationFunction()) {
            validationFunctions = List.of(this::validateIsNumber, getValidationFunction());
        } else {
            validationFunctions = List.of(this::validateIsNumber);
        }
        return validate(fieldValueModel, fieldModel, validationFunctions);
    }

    private Collection<String> validateIsNumber(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
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

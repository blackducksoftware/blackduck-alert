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

public class CheckboxConfigField extends ConfigField {
    public CheckboxConfigField(final String key, final String label, final String description, final boolean required, final String panel) {
        super(key, label, description, FieldType.CHECKBOX_INPUT.getFieldTypeName(), required, false, panel);
    }

    public CheckboxConfigField(final String key, final String label, final String description, final boolean required, final String panel, final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.CHECKBOX_INPUT.getFieldTypeName(), required, false, panel, validationFunction);
    }

    public CheckboxConfigField(final String key, final String label, final String description, final boolean required) {
        super(key, label, description, FieldType.CHECKBOX_INPUT.getFieldTypeName(), required, false);
    }

    public CheckboxConfigField(final String key, final String label, final String description, final boolean required, final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.CHECKBOX_INPUT.getFieldTypeName(), required, false, validationFunction);
    }

    public static CheckboxConfigField create(final String key, final String label, final String description) {
        return new CheckboxConfigField(key, label, description, false);
    }

    public static CheckboxConfigField create(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new CheckboxConfigField(key, label, description, false, validationFunction);
    }

    public static CheckboxConfigField createRequired(final String key, final String label, final String description) {
        return new CheckboxConfigField(key, label, description, true);
    }

    public static CheckboxConfigField createRequired(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new CheckboxConfigField(key, label, description, true, validationFunction);
    }

    public static CheckboxConfigField createPanel(final String key, final String label, final String description, final String panel) {
        return new CheckboxConfigField(key, label, description, false, panel);
    }

    public static CheckboxConfigField createPanel(final String key, final String label, final String description, final String panel, final ConfigValidationFunction validationFunction) {
        return new CheckboxConfigField(key, label, description, false, panel, validationFunction);
    }

    @Override
    public Collection<String> validate(final FieldValueModel fieldValueModel, final FieldModel fieldModel) {
        final List<ConfigValidationFunction> validationFunctions;
        if (null != getValidationFunction()) {
            validationFunctions = List.of(this::validateValueIsBoolean, getValidationFunction());
        } else {
            validationFunctions = List.of(this::validateValueIsBoolean);
        }
        return validate(fieldValueModel, fieldModel, validationFunctions);
    }

    private Collection<String> validateValueIsBoolean(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
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

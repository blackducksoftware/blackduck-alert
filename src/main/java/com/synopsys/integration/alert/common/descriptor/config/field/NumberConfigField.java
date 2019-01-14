/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import java.util.function.BiFunction;

import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;

public class NumberConfigField extends ConfigField {
    public static NumberConfigField create(final String key, final String label) {
        return new NumberConfigField(key, label, false, false);
    }

    public static NumberConfigField create(final String key, final String label, final BiFunction<FieldValueModel, FieldModel, Collection<String>> validationFunction) {
        return new NumberConfigField(key, label, false, false, validationFunction);
    }

    public static NumberConfigField createRequired(final String key, final String label) {
        return new NumberConfigField(key, label, true, false);
    }

    public static NumberConfigField createRequired(final String key, final String label, final BiFunction<FieldValueModel, FieldModel, Collection<String>> validationFunction) {
        return new NumberConfigField(key, label, true, false, validationFunction);
    }

    public static NumberConfigField createGrouped(final String key, final String label, final FieldGroup group) {
        return new NumberConfigField(key, label, false, false, group);
    }

    public static NumberConfigField createGrouped(final String key, final String label, final FieldGroup group, final BiFunction<FieldValueModel, FieldModel, Collection<String>> validationFunction) {
        return new NumberConfigField(key, label, false, false, group, validationFunction);
    }

    public NumberConfigField(final String key, final String label, final boolean required, final boolean sensitive, final FieldGroup group) {
        super(key, label, FieldType.NUMBER_INPUT.getFieldTypeName(), required, sensitive, group);
    }

    public NumberConfigField(final String key, final String label, final boolean required, final boolean sensitive, final FieldGroup group, final BiFunction<FieldValueModel, FieldModel, Collection<String>> validationFunction) {
        super(key, label, FieldType.NUMBER_INPUT.getFieldTypeName(), required, sensitive, group, validationFunction);
    }

    public NumberConfigField(final String key, final String label, final boolean required, final boolean sensitive) {
        super(key, label, FieldType.NUMBER_INPUT.getFieldTypeName(), required, sensitive);
    }

    public NumberConfigField(final String key, final String label, final boolean required, final boolean sensitive, final BiFunction<FieldValueModel, FieldModel, Collection<String>> validationFunction) {
        super(key, label, FieldType.NUMBER_INPUT.getFieldTypeName(), required, sensitive, validationFunction);
    }

    @Override
    public Collection<String> validate(final FieldValueModel fieldValueModel, final FieldModel fieldModel) {
        final List<BiFunction<FieldValueModel, FieldModel, Collection<String>>> validationFunctions;
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
                return List.of("Not an integer value");
            }
        }

        return List.of();
    }

}

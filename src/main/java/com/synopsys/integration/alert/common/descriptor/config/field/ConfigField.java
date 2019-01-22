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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.util.Stringable;

public class ConfigField extends Stringable {
    public static final String REQUIRED_FIELD_MISSING = "Required field missing";
    public static final ConfigValidationFunction NO_VALIDATION = (fieldToValidate, fieldModel) -> List.of();

    private String key;
    private String label;
    private String type;
    private boolean required;
    private boolean sensitive;
    private FieldGroup group;
    private String subGroup;
    private transient ConfigValidationFunction validationFunction;

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final FieldGroup group, final String subGroup,
        final ConfigValidationFunction validationFunction) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.required = required;
        this.sensitive = sensitive;
        this.group = group;
        this.subGroup = subGroup;
        this.validationFunction = validationFunction;
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final FieldGroup group) {
        this(key, label, type, required, sensitive, group, "", NO_VALIDATION);
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final FieldGroup group, final ConfigValidationFunction validationFunction) {
        this(key, label, type, required, sensitive, group, "", validationFunction);
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final String subGroup) {
        this(key, label, type, required, sensitive, FieldGroup.DEFAULT, subGroup, NO_VALIDATION);
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final String subGroup, final ConfigValidationFunction validationFunction) {
        this(key, label, type, required, sensitive, FieldGroup.DEFAULT, subGroup, validationFunction);
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive) {
        this(key, label, type, required, sensitive, FieldGroup.DEFAULT, "", NO_VALIDATION);
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final ConfigValidationFunction validationFunction) {
        this(key, label, type, required, sensitive, FieldGroup.DEFAULT, "", validationFunction);
    }

    public Collection<String> validate(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        return validate(fieldToValidate, fieldModel, List.of(validationFunction));
    }

    final Collection<String> validate(final FieldValueModel fieldToValidate, final FieldModel fieldModel, final List<ConfigValidationFunction> validationFunctions) {
        final Collection<String> errors = new LinkedList<>();
        errors.addAll(validateRequiredField(fieldToValidate));
        final boolean performValidation = errors.isEmpty() && fieldToValidate.hasValues();
        if (performValidation) {
            for (final ConfigValidationFunction validation : validationFunctions) {
                if (null != validation) {
                    errors.addAll(validation.apply(fieldToValidate, fieldModel));
                }
            }
        }

        return errors;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(final boolean sensitive) {
        this.sensitive = sensitive;
    }

    public FieldGroup getGroup() {
        return group;
    }

    public void setGroup(final FieldGroup group) {
        this.group = group;
    }

    public String getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(final String subGroup) {
        this.subGroup = subGroup;
    }

    public ConfigValidationFunction getValidationFunction() {
        return validationFunction;
    }

    public void setValidationFunction(final ConfigValidationFunction validationFunction) {
        this.validationFunction = validationFunction;
    }

    private Collection<String> validateRequiredField(final FieldValueModel fieldToValidate) {
        final Collection<String> errors = new LinkedList<>();
        if (isRequired()) {
            if (fieldToValidate.hasValues()) {
                final boolean valuesAllEmpty = fieldToValidate.getValues().stream().allMatch(value -> StringUtils.isBlank(value));
                if (valuesAllEmpty) {
                    errors.add(REQUIRED_FIELD_MISSING);
                }
            } else {
                if (!fieldToValidate.isSet()) {
                    errors.add(REQUIRED_FIELD_MISSING);
                }
            }
        }
        return errors;
    }
}

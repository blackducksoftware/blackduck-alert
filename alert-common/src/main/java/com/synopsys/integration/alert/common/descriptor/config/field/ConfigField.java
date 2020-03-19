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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class ConfigField extends AlertSerializableModel {
    public static final String REQUIRED_FIELD_MISSING = "Required field missing";
    public static final int MAX_FIELD_LENGTH = 511;
    public static final String FIELD_HEADER_EMPTY = "";
    public static final String FIELD_PANEL_DEFAULT = "";
    public static final String FIELD_LENGTH_LARGE = String.format("Field length is too large (Maximum length of %d).", MAX_FIELD_LENGTH);
    public static final ConfigValidationFunction NO_VALIDATION = (fieldToValidate, fieldModel) -> List.of();
    private final String description;
    private String key;
    private String label;
    private String type;
    private boolean required;
    private boolean sensitive;
    private boolean readOnly;
    private String panel;
    private String header;
    private Set<String> requiredRelatedFields;
    private Set<String> disallowedRelatedFields;
    private Set<String> defaultValues;
    private transient ConfigValidationFunction validationFunction;

    public ConfigField(final String key, final String label, final String description, final String type, final boolean required, final boolean sensitive, final boolean readOnly, final String panel, final String header,
        final ConfigValidationFunction validationFunction) {
        this.key = key;
        this.label = label;
        this.description = description;
        this.type = type;
        this.required = required;
        this.sensitive = sensitive;
        this.readOnly = readOnly;
        this.panel = panel;
        this.header = header;
        requiredRelatedFields = new HashSet<>();
        disallowedRelatedFields = new HashSet<>();
        this.validationFunction = validationFunction;
        defaultValues = new HashSet<>();
    }

    public ConfigField(final String key, final String label, final String description, final String type, final boolean required, final boolean sensitive, final String panel) {
        this(key, label, description, type, required, sensitive, false, panel, FIELD_HEADER_EMPTY, NO_VALIDATION);
    }

    public ConfigField(final String key, final String label, final String description, final String type, final boolean required, final boolean sensitive, final String panel, final ConfigValidationFunction validationFunction) {
        this(key, label, description, type, required, sensitive, false, panel, FIELD_HEADER_EMPTY, validationFunction);
    }

    public ConfigField(final String key, final String label, final String description, final String type, final boolean required, final boolean sensitive) {
        this(key, label, description, type, required, sensitive, false, FIELD_PANEL_DEFAULT, FIELD_HEADER_EMPTY, NO_VALIDATION);
    }

    public ConfigField(final String key, final String label, final String description, final String type, final boolean required, final boolean sensitive, final ConfigValidationFunction validationFunction) {
        this(key, label, description, type, required, sensitive, false, FIELD_PANEL_DEFAULT, FIELD_HEADER_EMPTY, validationFunction);
    }

    public Collection<String> validate(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        return validate(fieldToValidate, fieldModel, List.of(validationFunction));
    }

    final Collection<String> validate(final FieldValueModel fieldToValidate, final FieldModel fieldModel, final List<ConfigValidationFunction> validationFunctions) {
        final Collection<String> errors = new LinkedList<>();
        validateRequiredField(fieldToValidate, errors);
        validateLength(fieldToValidate, errors);
        if (errors.isEmpty()) {
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

    public String getDescription() {
        return description;
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

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getPanel() {
        return panel;
    }

    public ConfigField setPanel(final String panel) {
        this.panel = panel;
        return this;
    }

    public String getHeader() {
        return header;
    }

    public ConfigField setHeader(final String header) {
        this.header = header;
        return this;
    }

    public Set<String> getRequiredRelatedFields() {
        return requiredRelatedFields;
    }

    public void setRequiredRelatedFields(final Set<String> requiredRelatedFields) {
        this.requiredRelatedFields = requiredRelatedFields;
    }

    public ConfigField requireField(final String configFieldKey) {
        requiredRelatedFields.add(configFieldKey);
        return this;
    }

    public Set<String> getDisallowedRelatedFields() {
        return disallowedRelatedFields;
    }

    public void setDisallowedRelatedFields(final Set<String> disallowedRelatedFields) {
        this.disallowedRelatedFields = disallowedRelatedFields;
    }

    public ConfigField disallowField(final String configFieldKey) {
        disallowedRelatedFields.add(configFieldKey);
        return this;
    }

    public ConfigValidationFunction getValidationFunction() {
        return validationFunction;
    }

    public void setValidationFunction(final ConfigValidationFunction validationFunction) {
        this.validationFunction = validationFunction;
    }

    public ConfigField addDefaultValue(final String value) {
        defaultValues.add(value);
        return this;
    }

    public ConfigField addDefaultValues(final Set<String> values) {
        defaultValues.addAll(values);
        return this;
    }

    public Set<String> getDefaultValues() {
        return defaultValues;
    }

    public void setDefaultValues(final Set<String> defaultValues) {
        this.defaultValues = defaultValues;
    }

    private void validateRequiredField(final FieldValueModel fieldToValidate, final Collection<String> errors) {
        if (isRequired() && fieldToValidate.containsNoData()) {
            errors.add(REQUIRED_FIELD_MISSING);
        }
    }

    private void validateLength(final FieldValueModel fieldValueModel, final Collection<String> errors) {
        final Collection<String> values = fieldValueModel.getValues();
        if (null == values) {
            return;
        }

        final boolean tooLargeFound = values
                                          .stream()
                                          .filter(StringUtils::isNotBlank)
                                          .anyMatch(value -> MAX_FIELD_LENGTH < value.length());
        if (tooLargeFound) {
            errors.add(FIELD_LENGTH_LARGE);
        }
    }
}

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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.descriptor.config.field.validators.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

/**
 * <strong>Constructor Parameters:</strong><br/>
 * key         A string to uniquely identify this field throughout the application.<br/>
 * label       A human-readable name for this field.<br/>
 * description An explanation of what this field does.<br/>
 * type        An enum representation of the UI element that should be rendered for this field.<br/>
 * <br/>
 * <strong>Default values:</strong><br/>
 * - required = false<br/>
 * - sensitive = false<br/>
 * - readOnly = false<br/>
 * - panel = ConfigField.FIELD_PANEL_DEFAULT<br/>
 * - header = ConfigField.FIELD_HEADER_EMPTY<br/>
 * - requiredRelatedFields = new HashSet<>()<br/>
 * - disallowedRelatedFields = new HashSet<>()<br/>
 * - defaultValues = new HashSet<>()<br/>
 * - validationFunctions = new LinkedList<>()<br/>
 */
public abstract class ConfigField extends AlertSerializableModel {
    public static final String REQUIRED_FIELD_MISSING = "Required field missing";
    public static final int MAX_FIELD_LENGTH = 511;
    public static final String FIELD_HEADER_EMPTY = "";
    public static final String FIELD_PANEL_DEFAULT = "";
    public static final String FIELD_LENGTH_LARGE = String.format("Field length is too large (Maximum length of %d).", MAX_FIELD_LENGTH);

    private final String key;
    private final String label;
    private final String description;
    private final String type;

    private boolean required;
    private boolean sensitive;
    private boolean readOnly;
    private String panel;
    private String header;

    private Set<String> requiredRelatedFields;
    private Set<String> disallowedRelatedFields;
    private Set<String> defaultValues;
    private transient List<ConfigValidationFunction> validationFunctions;

    /**
     * @param key         A string to uniquely identify this field throughout the application.
     * @param label       A human-readable name for this field.
     * @param description An explanation of what this field does.
     * @param type        An enum representation of the UI element that should be rendered for this field.
     *                    <p/>
     *                    <strong>Default values:</strong><br/>
     *                    - required = false<br/>
     *                    - sensitive = false<br/>
     *                    - readOnly = false<br/>
     *                    - panel = ConfigField.FIELD_PANEL_DEFAULT<br/>
     *                    - header = ConfigField.FIELD_HEADER_EMPTY<br/>
     *                    - requiredRelatedFields = new HashSet<>()<br/>
     *                    - disallowedRelatedFields = new HashSet<>()<br/>
     *                    - defaultValues = new HashSet<>()<br/>
     *                    - validationFunctions = new LinkedList<>()<br/>
     */
    public ConfigField(String key, String label, String description, FieldType type) {
        this.key = key;
        this.label = label;
        this.description = description;
        this.type = type.getFieldTypeName();
        this.required = false;
        this.sensitive = false;
        this.readOnly = false;
        this.panel = FIELD_PANEL_DEFAULT;
        this.header = FIELD_HEADER_EMPTY;
        this.requiredRelatedFields = new HashSet<>();
        this.disallowedRelatedFields = new HashSet<>();
        this.defaultValues = new HashSet<>();
        this.validationFunctions = new LinkedList<>();
    }

    public ConfigField applyRequired(boolean required) {
        this.required = required;
        return this;
    }

    public ConfigField applySensitive(boolean sensitive) {
        this.sensitive = sensitive;
        return this;
    }

    public ConfigField applyReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    public ConfigField applyPanel(String panel) {
        this.panel = panel;
        return this;
    }

    public ConfigField applyHeader(String header) {
        this.header = header;
        return this;
    }

    public ConfigField applyRequiredRelatedFields(Set<String> requiredRelatedFields) {
        if (null != requiredRelatedFields) {
            this.requiredRelatedFields.addAll(requiredRelatedFields);
        }
        return this;
    }

    public ConfigField applyRequiredRelatedField(String requiredRelatedField) {
        if (null != requiredRelatedField) {
            this.requiredRelatedFields.add(requiredRelatedField);
        }
        return this;
    }

    public ConfigField applyDisallowedRelatedFields(Set<String> disallowedRelatedFields) {
        if (null != disallowedRelatedFields) {
            this.disallowedRelatedFields.addAll(disallowedRelatedFields);
        }
        return this;
    }

    public ConfigField applyDisallowedRelatedField(String disallowedRelatedField) {
        if (null != disallowedRelatedField) {
            this.disallowedRelatedFields.add(disallowedRelatedField);
        }
        return this;
    }

    public ConfigField applyDefaultValues(Set<String> defaultValues) {
        if (null != defaultValues) {
            this.defaultValues.addAll(defaultValues);
        }
        return this;
    }

    public ConfigField applyDefaultValue(String defaultValue) {
        if (null != defaultValue) {
            this.defaultValues.add(defaultValue);
        }
        return this;
    }

    public ConfigField applyValidationFunctions(List<ConfigValidationFunction> validationFunctions) {
        if (null != validationFunctions) {
            this.validationFunctions.addAll(validationFunctions);
        }
        return this;
    }

    public ConfigField applyValidationFunctions(ConfigValidationFunction... validationFunctions) {
        if (null != validationFunctions) {
            applyValidationFunctions(Arrays.stream(validationFunctions).collect(Collectors.toList()));
        }
        return this;
    }

    public ValidationResult validate(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        return validate(fieldToValidate, fieldModel, getValidationFunctions());
    }

    private ValidationResult validate(FieldValueModel fieldToValidate, FieldModel fieldModel, List<ConfigValidationFunction> validationFunctions) {
        ValidationResult errors = ValidationResult.of(validateRequiredField(fieldToValidate), validateLength(fieldToValidate));

        if (!errors.hasErrors()) {
            for (ConfigValidationFunction validation : validationFunctions) {
                if (null != validation) {
                    //errors.addAll(validation.apply(fieldToValidate, fieldModel));
                    errors = ValidationResult.of(validation.apply(fieldToValidate, fieldModel));
                }
            }
        }
        return errors;
    }

    protected void createValidators(List<ConfigValidationFunction> fieldDefaultValidators, ConfigValidationFunction[] validationFunctions) {
        if (null == validationFunctions) {
            this.applyValidationFunctions(fieldDefaultValidators);
        } else {
            List<ConfigValidationFunction> validators = Stream.concat(fieldDefaultValidators.stream(), Arrays.stream(validationFunctions)).collect(Collectors.toUnmodifiableList());
            this.applyValidationFunctions(validators);
        }
    }

    public String getKey() {
        return key;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getPanel() {
        return panel;
    }

    public String getHeader() {
        return header;
    }

    public Set<String> getRequiredRelatedFields() {
        return requiredRelatedFields;
    }

    public Set<String> getDisallowedRelatedFields() {
        return disallowedRelatedFields;
    }

    public List<ConfigValidationFunction> getValidationFunctions() {
        return validationFunctions;
    }

    public Set<String> getDefaultValues() {
        return defaultValues;
    }

    public void setRequiredRelatedFields(Set<String> requiredRelatedFields) {
        this.requiredRelatedFields = requiredRelatedFields;
    }

    public void setDisallowedRelatedFields(Set<String> disallowedRelatedFields) {
        this.disallowedRelatedFields = disallowedRelatedFields;
    }

    public void setDefaultValues(Set<String> defaultValues) {
        this.defaultValues = defaultValues;
    }

    private ValidationResult validateRequiredField(FieldValueModel fieldToValidate) {
        if (isRequired() && fieldToValidate.containsNoData()) {
            return ValidationResult.of(REQUIRED_FIELD_MISSING);
        }
        return ValidationResult.of();
    }

    private ValidationResult validateLength(FieldValueModel fieldValueModel) {
        Collection<String> values = fieldValueModel.getValues();
        if (null == values) {
            return ValidationResult.of();
        }

        boolean tooLargeFound = values
                                    .stream()
                                    .filter(StringUtils::isNotBlank)
                                    .anyMatch(value -> MAX_FIELD_LENGTH < value.length());
        if (tooLargeFound) {
            return ValidationResult.of(FIELD_LENGTH_LARGE);
        }
        return ValidationResult.of();
    }

}

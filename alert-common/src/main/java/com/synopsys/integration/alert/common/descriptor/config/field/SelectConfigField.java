/**
 * alert-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class SelectConfigField extends ConfigField {
    public static final String INVALID_OPTION_SELECTED = "Invalid option selected";
    private Collection<LabelValueSelectOption> options;
    private boolean searchable;
    private boolean multiSelect;

    public SelectConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final boolean readOnly, final boolean searchable, final boolean multiSelect,
        final Collection<LabelValueSelectOption> options) {
        super(key, label, description, FieldType.SELECT.getFieldTypeName(), required, sensitive, readOnly, ConfigField.FIELD_PANEL_DEFAULT, ConfigField.FIELD_HEADER_EMPTY, ConfigField.NO_VALIDATION);
        this.searchable = searchable;
        this.multiSelect = multiSelect;
        this.options = options;
    }

    public SelectConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final boolean readOnly, final boolean searchable, final boolean multiSelect,
        final Collection<LabelValueSelectOption> options,
        final ConfigValidationFunction validationFunction) {
        super(key, label, description, FieldType.SELECT.getFieldTypeName(), required, sensitive, readOnly, ConfigField.FIELD_PANEL_DEFAULT, ConfigField.FIELD_HEADER_EMPTY, validationFunction);
        this.searchable = searchable;
        this.multiSelect = multiSelect;
        this.options = options;
    }

    public SelectConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final Collection<LabelValueSelectOption> options) {
        this(key, label, description, required, sensitive, false, true, false, options);
    }

    public SelectConfigField(final String key, final String label, final String description, final boolean required, final boolean sensitive, final Collection<LabelValueSelectOption> options,
        final ConfigValidationFunction validationFunction) {
        this(key, label, description, required, sensitive, false, true, false, options, validationFunction);
    }

    public static SelectConfigField createEmpty(final String key, final String label, final String description) {
        return new SelectConfigField(key, label, description, false, false, List.of());
    }

    public static SelectConfigField createEmpty(final String key, final String label, final String description, final ConfigValidationFunction validationFunction) {
        return new SelectConfigField(key, label, description, false, false, List.of(), validationFunction);
    }

    public static SelectConfigField createRequired(final String key, final String label, final String description, final Collection<LabelValueSelectOption> options) {
        return new SelectConfigField(key, label, description, true, false, options);
    }

    public static SelectConfigField createRequired(final String key, final String label, final String description, final boolean searchable, final boolean multiSelect, final Collection<LabelValueSelectOption> options,
        final ConfigValidationFunction validationFunction) {
        return new SelectConfigField(key, label, description, true, false, false, searchable, multiSelect, options, validationFunction);
    }

    public static SelectConfigField createRequired(final String key, final String label, final String description, final boolean searchable, final boolean multiSelect, final Collection<LabelValueSelectOption> options) {
        return new SelectConfigField(key, label, description, true, false, false, searchable, multiSelect, options);
    }

    public static SelectConfigField createRequired(final String key, final String label, final String description, final Collection<LabelValueSelectOption> options, final ConfigValidationFunction validationFunction) {
        return new SelectConfigField(key, label, description, true, false, options, validationFunction);
    }

    public static SelectConfigField create(final String key, final String label, final String description, final Collection<LabelValueSelectOption> options) {
        return new SelectConfigField(key, label, description, false, false, options);
    }

    public static SelectConfigField create(final String key, final String label, final String description, final Collection<LabelValueSelectOption> options, final boolean isMultiSelect) {
        return new SelectConfigField(key, label, description, false, false, false, true, isMultiSelect, options);
    }

    public static SelectConfigField create(final String key, final String label, final String description, final Collection<LabelValueSelectOption> options, final ConfigValidationFunction validationFunction) {
        return new SelectConfigField(key, label, description, false, false, options, validationFunction);
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(final boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(final boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public Collection<LabelValueSelectOption> getOptions() {
        return options;
    }

    public void setOptions(final Collection<LabelValueSelectOption> options) {
        this.options = options;
    }

    @Override
    public Collection<String> validate(final FieldValueModel fieldValueModel, final FieldModel fieldModel) {
        final List<ConfigValidationFunction> validationFunctions;
        if (null != getValidationFunction()) {
            validationFunctions = List.of(this::validateIsValidOption, getValidationFunction());
        } else {
            validationFunctions = List.of(this::validateIsValidOption);
        }
        return validate(fieldValueModel, fieldModel, validationFunctions);
    }

    private Collection<String> validateIsValidOption(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final Collection<LabelValueSelectOption> fieldOptions = getOptions();
        if (fieldToValidate.hasValues() && !fieldOptions.isEmpty()) {
            final boolean doesMatchKnownReferral = fieldToValidate.getValues()
                                                       .stream()
                                                       .map(StringUtils::trimToEmpty)
                                                       .allMatch(value -> fieldOptions
                                                                              .stream()
                                                                              .map(LabelValueSelectOption::getValue)
                                                                              .anyMatch(fieldOption -> fieldOption.equalsIgnoreCase(value)));
            if (!doesMatchKnownReferral) {
                return List.of(INVALID_OPTION_SELECTED);
            }
        }
        return List.of();
    }
}

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

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.validators.ConfigValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class SelectConfigField extends ConfigField {
    public static final String INVALID_OPTION_SELECTED = "Invalid option selected";
    private Collection<LabelValueSelectOption> options;
    private boolean searchable;
    private boolean multiSelect;
    private boolean removeSelected;
    private boolean clearable;

    protected SelectConfigField(String key, String label, String description, FieldType fieldType, boolean required, boolean searchable, boolean multiSelect, boolean removeSelected, boolean clearable) {
        super(key, label, description, fieldType, required, false, false, ConfigField.FIELD_PANEL_DEFAULT, ConfigField.FIELD_HEADER_EMPTY);
        this.searchable = searchable;
        this.multiSelect = multiSelect;
        this.removeSelected = removeSelected;
        this.clearable = clearable;
        options = List.of();
        createValidators(List.of(this::validateIsValidOption), null);
    }

    public SelectConfigField(String key, String label, String description, boolean required, boolean sensitive, boolean readOnly, boolean searchable, boolean multiSelect,
        boolean removeSelected, boolean clearable, Collection<LabelValueSelectOption> options) {
        super(key, label, description, FieldType.SELECT, required, sensitive, readOnly, ConfigField.FIELD_PANEL_DEFAULT, ConfigField.FIELD_HEADER_EMPTY);
        this.searchable = searchable;
        this.multiSelect = multiSelect;
        this.options = options;
        this.removeSelected = removeSelected;
        this.clearable = clearable;
        createValidators(List.of(this::validateIsValidOption), null);
    }

    public SelectConfigField(String key, String label, String description, boolean required, boolean sensitive, boolean readOnly, boolean searchable, boolean multiSelect,
        boolean removeSelected, boolean clearable, Collection<LabelValueSelectOption> options, ConfigValidationFunction... validationFunctions) {
        super(key, label, description, FieldType.SELECT, required, sensitive, readOnly, ConfigField.FIELD_PANEL_DEFAULT, ConfigField.FIELD_HEADER_EMPTY);
        this.searchable = searchable;
        this.multiSelect = multiSelect;
        this.options = options;
        this.removeSelected = removeSelected;
        this.clearable = clearable;
        createValidators(List.of(this::validateIsValidOption), validationFunctions);

    }

    public static SelectConfigField createEmpty(String key, String label, String description) {
        return new SelectConfigField(key, label, description, false, false, false, true, false, true, true, List.of());
    }

    public static SelectConfigField createEmpty(String key, String label, String description, ConfigValidationFunction... validationFunctions) {
        return new SelectConfigField(key, label, description, false, false, false, true, false, true, true, List.of(), validationFunctions);
    }

    public static SelectConfigField createRequired(String key, String label, String description, Collection<LabelValueSelectOption> options) {
        return new SelectConfigField(key, label, description, true, false, false, true, false, true, true, options);
    }

    public static SelectConfigField createRequired(String key, String label, String description, boolean searchable, boolean multiSelect, Collection<LabelValueSelectOption> options, ConfigValidationFunction... validationFunctions) {
        return new SelectConfigField(key, label, description, true, false, false, searchable, multiSelect, true, true, options, validationFunctions);
    }

    public static SelectConfigField createRequired(String key, String label, String description, boolean searchable, boolean multiSelect, Collection<LabelValueSelectOption> options) {
        return new SelectConfigField(key, label, description, true, false, false, searchable, multiSelect, true, true, options);
    }

    public static SelectConfigField createRequired(String key, String label, String description, Collection<LabelValueSelectOption> options, ConfigValidationFunction... validationFunctions) {
        return new SelectConfigField(key, label, description, true, false, false, true, false, true, true, options, validationFunctions);
    }

    public static SelectConfigField createRequired(String key, String label, String description, boolean searchable, boolean multiSelect, boolean removeSelected,
        Collection<LabelValueSelectOption> options) {
        return new SelectConfigField(key, label, description, true, false, false, searchable, multiSelect, removeSelected, true, options);
    }

    public static SelectConfigField createRequired(String key, String label, String description, boolean removeSelected, Collection<LabelValueSelectOption> options, ConfigValidationFunction... validationFunctions) {
        return new SelectConfigField(key, label, description, true, false, false, true, false, removeSelected, true, options, validationFunctions);
    }

    public static SelectConfigField create(String key, String label, String description, Collection<LabelValueSelectOption> options) {
        return new SelectConfigField(key, label, description, false, false, false, true, false, true, true, options);
    }

    public static SelectConfigField create(String key, String label, String description, boolean removeSelected, boolean clearable, Collection<LabelValueSelectOption> options) {
        return new SelectConfigField(key, label, description, false, false, false, true, false, removeSelected, clearable, options);
    }

    public static SelectConfigField create(String key, String label, String description, boolean removeSelected, boolean clearable, Collection<LabelValueSelectOption> options, ConfigValidationFunction... validationFunctions) {
        return new SelectConfigField(key, label, description, false, false, false, true, false, removeSelected, clearable, options, validationFunctions);
    }

    public static SelectConfigField create(String key, String label, String description, Collection<LabelValueSelectOption> options, boolean isMultiSelect) {
        return new SelectConfigField(key, label, description, false, false, false, true, isMultiSelect, true, true, options);
    }

    public static SelectConfigField create(String key, String label, String description, Collection<LabelValueSelectOption> options, ConfigValidationFunction... validationFunctions) {
        return new SelectConfigField(key, label, description, false, false, false, true, false, true, true, options, validationFunctions);
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
    }

    public Collection<LabelValueSelectOption> getOptions() {
        return options;
    }

    public void setOptions(Collection<LabelValueSelectOption> options) {
        this.options = options;
    }

    public boolean isRemoveSelected() {
        return removeSelected;
    }

    public void setRemoveSelected(boolean removeSelected) {
        this.removeSelected = removeSelected;
    }

    public boolean isClearable() {
        return clearable;
    }

    public void setClearable(final boolean clearable) {
        this.clearable = clearable;
    }

    private Collection<String> validateIsValidOption(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Collection<LabelValueSelectOption> fieldOptions = getOptions();
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

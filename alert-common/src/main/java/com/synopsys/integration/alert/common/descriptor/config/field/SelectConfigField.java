/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class SelectConfigField extends ConfigField {
    public static final String INVALID_OPTION_SELECTED = "Invalid option selected";
    private static final long serialVersionUID = -3122384908284112253L;

    private final Collection<LabelValueSelectOption> options;

    private boolean searchable;
    private boolean multiSelect;
    private boolean removeSelected;
    private boolean clearable;

    public SelectConfigField(String key, String label, String description, Collection<LabelValueSelectOption> options) {
        this(key, label, description, FieldType.SELECT, options);
    }

    protected SelectConfigField(String key, String label, String description, FieldType fieldType, Collection<LabelValueSelectOption> options) {
        super(key, label, description, fieldType);
        this.options = options;
        this.searchable = true;
        this.multiSelect = false;
        this.removeSelected = true;
        this.clearable = true;
        createValidators(List.of(this::validateIsValidOption), null);
    }

    public SelectConfigField applySearchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public SelectConfigField applyMultiSelect(boolean multiSelect) {
        this.multiSelect = multiSelect;
        return this;
    }

    public SelectConfigField applyRemoveSelected(boolean removeSelected) {
        this.removeSelected = removeSelected;
        return this;
    }

    public SelectConfigField applyClearable(boolean clearable) {
        this.clearable = clearable;
        return this;
    }

    public Collection<LabelValueSelectOption> getOptions() {
        return options;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public boolean isMultiSelect() {
        return multiSelect;
    }

    public boolean isRemoveSelected() {
        return removeSelected;
    }

    public boolean isClearable() {
        return clearable;
    }

    private ValidationResult validateIsValidOption(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Collection<LabelValueSelectOption> fieldOptions = getOptions();
        if (fieldToValidate.hasValues() && !fieldOptions.isEmpty()) {
            boolean doesMatchKnownReferral = fieldToValidate.getValues()
                                                 .stream()
                                                 .map(StringUtils::trimToEmpty)
                                                 .allMatch(value -> fieldOptions
                                                                        .stream()
                                                                        .map(LabelValueSelectOption::getValue)
                                                                        .anyMatch(fieldOption -> fieldOption.equalsIgnoreCase(value)));
            if (!doesMatchKnownReferral) {
                return ValidationResult.errors(INVALID_OPTION_SELECTED);
            }
        }
        return ValidationResult.success();
    }

}

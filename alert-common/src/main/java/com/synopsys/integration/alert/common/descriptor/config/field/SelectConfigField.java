/**
 * alert-common
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class SelectConfigField extends ConfigField {
    public static final String INVALID_OPTION_SELECTED = "Invalid option selected";
    private Collection<String> options;
    private boolean searchable;
    private boolean multiSelect;

    public SelectConfigField(final String key, final String label, final boolean required, final boolean sensitive, final boolean searchable, final boolean multiSelect, final Collection<String> options) {
        super(key, label, FieldType.SELECT.getFieldTypeName(), required, sensitive, FieldGroup.DEFAULT, "", ConfigField.NO_VALIDATION);
        this.searchable = searchable;
        this.multiSelect = multiSelect;
        this.options = options;
    }

    public SelectConfigField(final String key, final String label, final boolean required, final boolean sensitive, final boolean searchable, final boolean multiSelect, final Collection<String> options,
        final ConfigValidationFunction validationFunction) {
        super(key, label, FieldType.SELECT.getFieldTypeName(), required, sensitive, FieldGroup.DEFAULT, "", validationFunction);
        this.searchable = searchable;
        this.multiSelect = multiSelect;
        this.options = options;
    }

    public SelectConfigField(final String key, final String label, final boolean required, final boolean sensitive, final Collection<String> options) {
        this(key, label, required, sensitive, true, false, options);
    }

    public SelectConfigField(final String key, final String label, final boolean required, final boolean sensitive, final Collection<String> options, final ConfigValidationFunction validationFunction) {
        this(key, label, required, sensitive, true, false, options, validationFunction);
    }

    public static SelectConfigField createEmpty(final String key, final String label) {
        return new SelectConfigField(key, label, false, false, Collections.emptyList());
    }

    public static SelectConfigField createEmpty(final String key, final String label, final ConfigValidationFunction validationFunction) {
        return new SelectConfigField(key, label, false, false, Collections.emptyList(), validationFunction);
    }

    public static SelectConfigField createRequired(final String key, final String label, final Collection<String> options) {
        return new SelectConfigField(key, label, true, false, options);
    }

    public static SelectConfigField createRequired(final String key, final String label, final Collection<String> options, final ConfigValidationFunction validationFunction) {
        return new SelectConfigField(key, label, true, false, options, validationFunction);
    }

    public static SelectConfigField create(final String key, final String label, final Collection<String> options) {
        return new SelectConfigField(key, label, false, false, options);
    }

    public static SelectConfigField create(final String key, final String label, final Collection<String> options, final ConfigValidationFunction validationFunction) {
        return new SelectConfigField(key, label, false, false, options, validationFunction);
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

    public Collection<String> getOptions() {
        return options;
    }

    public void setOptions(final Collection<String> options) {
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
        final Collection<String> fieldOptions = getOptions();
        if (fieldToValidate.hasValues() && !fieldOptions.isEmpty()) {
            final boolean doesMatchKnownReferral = fieldToValidate.getValues()
                                                       .stream()
                                                       .map(StringUtils::trimToEmpty)
                                                       .allMatch(value -> fieldOptions
                                                                              .stream()
                                                                              .anyMatch(fieldOption -> fieldOption.equalsIgnoreCase(value)));
            if (!doesMatchKnownReferral) {
                return List.of(INVALID_OPTION_SELECTED);
            }
        }
        return List.of();
    }
}

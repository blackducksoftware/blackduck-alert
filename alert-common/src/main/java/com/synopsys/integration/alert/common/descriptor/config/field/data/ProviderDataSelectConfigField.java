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
package com.synopsys.integration.alert.common.descriptor.config.field.data;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigValidationFunction;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class ProviderDataSelectConfigField extends ProviderDataField {
    private String providerDataEndpoint;
    private boolean searchable = true;
    private boolean multiSelect = false;

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, boolean readOnly, String panel, String header, ConfigValidationFunction validationFunction,
        String providerDataEndpoint, boolean searchable, boolean multiSelect) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, readOnly, panel, header, validationFunction);
        this.providerDataEndpoint = providerDataEndpoint;
        this.searchable = searchable;
        this.multiSelect = multiSelect;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, boolean readOnly, String panel, String header, ConfigValidationFunction validationFunction,
        String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, readOnly, panel, header, validationFunction);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, String panel, String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, panel);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, String panel, ConfigValidationFunction validationFunction, String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, panel, validationFunction);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public ProviderDataSelectConfigField(String key, String label, String description, boolean required, boolean sensitive, ConfigValidationFunction validationFunction, String providerDataEndpoint) {
        super(key, label, description, FieldType.PROVIDER_DATA_SELECT.getFieldTypeName(), required, sensitive, validationFunction);
        this.providerDataEndpoint = providerDataEndpoint;
    }

    public static ProviderDataSelectConfigField create(String key, String label, String description, String providerDataEndpoint, boolean isMultiSelect) {
        return new ProviderDataSelectConfigField(
            key, label, description, false, false, false, ConfigField.FIELD_PANEL_DEFAULT, ConfigField.FIELD_HEADER_EMPTY, ConfigField.NO_VALIDATION, providerDataEndpoint, true, isMultiSelect);
    }

    public String getProviderDataEndpoint() {
        return providerDataEndpoint;
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

}

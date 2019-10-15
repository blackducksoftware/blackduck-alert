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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.HashSet;
import java.util.Set;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointSelectField extends SelectConfigField {
    private String endpoint;
    private Set<String> returnedDataFieldKeys;

    public static EndpointSelectField create(final String key, final String label, final String description, boolean searchable, boolean multiSelect, boolean removeSelected, boolean clearable) {
        return new EndpointSelectField(key, label, description, false, searchable, multiSelect, removeSelected, clearable);
    }

    public static EndpointSelectField createRequired(final String key, final String label, final String description, boolean searchable, boolean multiSelect, boolean removeSelected, boolean clearable) {
        return new EndpointSelectField(key, label, description, true, searchable, multiSelect, removeSelected, clearable);
    }

    public static EndpointSelectField createRequired(final String key, final String label, final String description) {
        return new EndpointSelectField(key, label, description, true, true, true, true, true);
    }

    public EndpointSelectField(final String key, final String label, final String description, final boolean required, boolean searchable, boolean multiSelect, boolean removeSelected, boolean clearable) {
        super(key, label, description, FieldType.ENDPOINT_SELECT, required, searchable, multiSelect, removeSelected, clearable);
        this.endpoint = CustomEndpointManager.CUSTOM_ENDPOINT_URL;
        this.returnedDataFieldKeys = new HashSet<>();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Set<String> getReturnedDataFieldKeys() {
        return returnedDataFieldKeys;
    }

    public ConfigField addDataKey(String key) {
        returnedDataFieldKeys.add(key);
        return this;
    }
}

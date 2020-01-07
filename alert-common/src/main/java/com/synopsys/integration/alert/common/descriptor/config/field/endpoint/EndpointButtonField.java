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
package com.synopsys.integration.alert.common.descriptor.config.field.endpoint;

import java.util.ArrayList;
import java.util.List;

import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.FieldType;

public class EndpointButtonField extends EndpointField {
    private final Boolean successBox;
    private final List<ConfigField> subFields;

    public static EndpointButtonField create(final String key, final String label, final String description, final String buttonLabel) {
        return new EndpointButtonField(key, label, description, false, buttonLabel);
    }

    public static EndpointButtonField createRequired(final String key, final String label, final String description, final String buttonLabel) {
        return new EndpointButtonField(key, label, description, true, buttonLabel);
    }

    public static EndpointButtonField createWithSuccessBox(final String key, final String label, final String description, final String buttonLabel) {
        return new EndpointButtonField(key, label, description, false, buttonLabel, true);
    }

    public static EndpointButtonField createRequiredWithSuccessBox(final String key, final String label, final String description, final String buttonLabel) {
        return new EndpointButtonField(key, label, description, true, buttonLabel, true);
    }

    private EndpointButtonField(final String key, final String label, final String description, final boolean required, final String buttonLabel, final Boolean successBox,
        final List<ConfigField> subFields) {
        super(key, label, description, FieldType.ENDPOINT_BUTTON, required, false, buttonLabel, CustomEndpointManager.CUSTOM_ENDPOINT_URL);
        this.successBox = successBox;
        this.subFields = subFields;
    }

    public EndpointButtonField(final String key, final String label, final String description, final boolean required, final String buttonLabel, final Boolean successBox) {
        this(key, label, description, required, buttonLabel, successBox, new ArrayList<>());
    }

    public EndpointButtonField(final String key, final String label, final String description, final boolean required, final String buttonLabel) {
        this(key, label, description, required, buttonLabel, false, new ArrayList<>());
    }

    public EndpointButtonField addSubField(final ConfigField field) {
        if (!(field instanceof EndpointButtonField)) {
            subFields.add(field);
        }
        return this;
    }

    public Boolean getSuccessBox() {
        return successBox;
    }

    public List<ConfigField> getSubFields() {
        return subFields;
    }

}

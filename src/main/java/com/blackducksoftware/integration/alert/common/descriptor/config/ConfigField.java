/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.alert.common.descriptor.config;

import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.alert.common.enumeration.FieldGroup;
import com.blackducksoftware.integration.alert.common.enumeration.FieldType;

public class ConfigField {
    private String key;
    private String label;
    private FieldType type;
    private boolean required;
    private boolean sensitive;
    private FieldGroup group;
    private String subGroup;
    private List<String> options;

    public ConfigField(final String key, final String label, final FieldType type, final boolean required, final boolean sensitive, final FieldGroup group, final String subGroup, final List<String> options) {
        super();
        this.key = key;
        this.label = label;
        this.type = type;
        this.required = required;
        this.sensitive = sensitive;
        this.group = group;
        this.subGroup = subGroup;
        this.options = options;
    }

    public ConfigField(final String key, final String label, final FieldType type, final boolean required, final boolean sensitive, final FieldGroup group, final String subGroup) {
        this(key, label, type, required, sensitive, group, subGroup, Arrays.asList());
    }

    public ConfigField(final String key, final String label, final FieldType type, final boolean required, final boolean sensitive, final FieldGroup group) {
        this(key, label, type, required, sensitive, group, "");
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

    public FieldType getType() {
        return type;
    }

    public void setType(final FieldType type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public void setSensitive(final boolean sensitive) {
        this.sensitive = sensitive;
    }

    public boolean isSensitive() {
        return sensitive;
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

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(final List<String> options) {
        this.options = options;
    }

}

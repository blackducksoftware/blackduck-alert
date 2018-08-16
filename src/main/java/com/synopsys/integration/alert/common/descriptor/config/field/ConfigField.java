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
package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.enumeration.FieldGroup;
import com.synopsys.integration.util.Stringable;

public class ConfigField extends Stringable {
    private String key;
    private String label;
    private String type;
    private boolean required;
    private boolean sensitive;
    private FieldGroup group;
    private String subGroup;

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final FieldGroup group, final String subGroup) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.required = required;
        this.sensitive = sensitive;
        this.group = group;
        this.subGroup = subGroup;
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final FieldGroup group) {
        this(key, label, type, required, sensitive, group, "");
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive, final String subGroup) {
        this(key, label, type, required, sensitive, FieldGroup.DEFAULT, subGroup);
    }

    public ConfigField(final String key, final String label, final String type, final boolean required, final boolean sensitive) {
        this(key, label, type, required, sensitive, FieldGroup.DEFAULT, "");
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

}

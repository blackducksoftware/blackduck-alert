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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DescriptorMetadata extends AlertSerializableModel {
    private final String label;
    private final String urlName;
    private final String name;
    private final String description;
    private final DescriptorType type;
    private final ConfigContextEnum context;
    private final String fontAwesomeIcon;
    private final boolean automaticallyGenerateUI;
    private List<ConfigField> fields;
    private String testFieldLabel;

    public DescriptorMetadata(final String label, final String urlName, final String name, final String description, final ConfigContextEnum context, final String fontAwesomeIcon, final List<ConfigField> fields,
        final String testFieldLabel, final DescriptorType type) {
        this(label, urlName, name, description, type, context, fontAwesomeIcon, true, fields, testFieldLabel);
    }

    public DescriptorMetadata(final String label, final String urlName, final String name, final String description, final DescriptorType type, final ConfigContextEnum context, final String fontAwesomeIcon,
        final boolean automaticallyGenerateUI, final List<ConfigField> fields, final String testFieldLabel) {
        this.label = label;
        this.urlName = urlName;
        this.name = name;
        this.description = description;
        this.type = type;
        this.context = context;
        this.fontAwesomeIcon = fontAwesomeIcon;
        this.automaticallyGenerateUI = automaticallyGenerateUI;
        this.fields = fields;
        this.testFieldLabel = testFieldLabel;
    }

    public String getLabel() {
        return label;
    }

    public String getUrlName() {
        return urlName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public DescriptorType getType() {
        return type;
    }

    public ConfigContextEnum getContext() {
        return context;
    }

    public String getFontAwesomeIcon() {
        return fontAwesomeIcon;
    }

    public boolean isAutomaticallyGenerateUI() {
        return automaticallyGenerateUI;
    }

    public List<ConfigField> getFields() {
        return fields;
    }

    public void setFields(final List<ConfigField> fields) {
        this.fields = fields;
    }

    public String getTestFieldLabel() {
        return testFieldLabel;
    }

    public void setTestFieldLabel(final String testFieldLabel) {
        this.testFieldLabel = testFieldLabel;
    }
}

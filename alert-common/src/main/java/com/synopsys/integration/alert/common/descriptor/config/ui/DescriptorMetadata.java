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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;
import java.util.Set;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.AccessOperation;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DescriptorMetadata extends AlertSerializableModel {
    private static final long serialVersionUID = -6213193510077419010L;
    private final String label;
    private final String urlName;
    private final String name;
    private final String description;
    private final DescriptorType type;
    private final ConfigContextEnum context;
    private final boolean automaticallyGenerateUI;
    private final String componentNamespace;
    private List<ConfigField> fields;
    private Set<AccessOperation> operations;
    private boolean readOnly;
    private List<ConfigField> testFields;

    public DescriptorMetadata(DescriptorKey descriptorKey, String label, String urlName, String description, DescriptorType type, ConfigContextEnum context,
        boolean automaticallyGenerateUI, String componentNamespace, List<ConfigField> fields, List<ConfigField> testFields) {
        this.label = label;
        this.urlName = urlName;
        this.name = descriptorKey.getUniversalKey();
        this.description = description;
        this.type = type;
        this.context = context;
        this.automaticallyGenerateUI = automaticallyGenerateUI;
        this.componentNamespace = componentNamespace;
        this.fields = fields;
        this.operations = Set.of();
        this.testFields = testFields;
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

    public boolean isAutomaticallyGenerateUI() {
        return automaticallyGenerateUI;
    }

    public String getComponentNamespace() {
        return componentNamespace;
    }

    public List<ConfigField> getFields() {
        return fields;
    }

    public void setFields(List<ConfigField> fields) {
        this.fields = fields;
    }

    public Set<AccessOperation> getOperations() {
        return operations;
    }

    public void setOperations(Set<AccessOperation> operations) {
        this.operations = operations;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public List<ConfigField> getTestFields() {
        return testFields;
    }

    public void setTestFields(List<ConfigField> testFields) {
        this.testFields = testFields;
    }
}

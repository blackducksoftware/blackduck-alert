/**
 * blackduck-alert
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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.util.Stringable;

public class DescriptorMetadata extends Stringable {
    private final String label;
    private final String urlName;
    private final String name;
    private final DescriptorType type;
    private final ConfigContextEnum context;
    private final String fontAwesomeIcon;
    private final boolean automaticallyGenerateUI;
    private List<ConfigField> fields;

    public DescriptorMetadata(final String label, final String urlName, final String name, final DescriptorType type, final ConfigContextEnum context, final String fontAwesomeIcon, final List<ConfigField> fields) {
        this(label, urlName, name, type, context, fontAwesomeIcon, true, fields);
    }

    public DescriptorMetadata(final String label, final String urlName, final String name, final DescriptorType type, final ConfigContextEnum context, final String fontAwesomeIcon, final boolean automaticallyGenerateUI,
        final List<ConfigField> fields) {
        this.label = label;
        this.urlName = urlName;
        this.name = name;
        this.type = type;
        this.context = context;
        this.fontAwesomeIcon = fontAwesomeIcon;
        this.automaticallyGenerateUI = automaticallyGenerateUI;
        this.fields = fields;
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
}

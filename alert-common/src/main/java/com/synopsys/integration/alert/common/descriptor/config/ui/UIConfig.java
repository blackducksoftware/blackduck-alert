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
package com.synopsys.integration.alert.common.descriptor.config.ui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.util.Stringable;

public abstract class UIConfig extends Stringable {
    private final String label;
    private final String description;
    private final String urlName;
    private final String componentNamespace;

    protected UIConfig(String label, String description, String urlName, String componentNamespace) {
        this.label = label;
        this.description = description;
        this.urlName = urlName;
        this.componentNamespace = componentNamespace;
    }

    public UIConfig(String label, String description, String urlName) {
        this(label, description, urlName, "");
    }

    public abstract List<ConfigField> createFields();

    public String createTestLabel() {
        return "";
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getUrlName() {
        return urlName;
    }

    public boolean autoGenerateUI() {
        return StringUtils.isBlank(getComponentNamespace());
    }

    public String getComponentNamespace() {
        return componentNamespace;
    }
}

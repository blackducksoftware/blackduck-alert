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
package com.synopsys.integration.alert.common.descriptor.config;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.util.Stringable;

public class UIComponent extends Stringable {
    private final String label;
    private final String urlName;
    private final String descriptorName;
    private final String fontAwesomeIcon;
    private final List<ConfigField> fields;

    public UIComponent(final String label, final String urlName, final String descriptorName, final String fontAwesomeIcon, final List<ConfigField> fields) {
        this.label = label;
        this.urlName = urlName;
        this.descriptorName = descriptorName;
        this.fontAwesomeIcon = fontAwesomeIcon;
        this.fields = fields;
    }

    public String getLabel() {
        return label;
    }

    public String getUrlName() {
        return urlName;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public String getFontAwesomeIcon() {
        return fontAwesomeIcon;
    }

    public List<ConfigField> getFields() {
        return fields;
    }

}

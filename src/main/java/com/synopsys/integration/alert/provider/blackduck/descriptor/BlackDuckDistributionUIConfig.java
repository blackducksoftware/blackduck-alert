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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIComponent;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class BlackDuckDistributionUIConfig extends UIConfig {

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Black Duck", "blackduck", BlackDuckProvider.COMPONENT_NAME, "laptop", createFields());
    }

    public List<ConfigField> createFields() {
        final ConfigField filterByProject = CheckboxConfigField.create(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT, "Filter by project");
        final ConfigField projectNamePattern = TextInputConfigField.create(BlackDuckDescriptor.KEY_PROJECT_NAME_PATTERN, "Project name pattern");

        // TODO figure out how to create a project listing (Perhaps a new field type called table)
        // TODO create a linkedField that is an endpoint the UI hits to generate a field
        final ConfigField configuredProject = SelectConfigField.createRequiredEmpty(BlackDuckDescriptor.KEY_CONFIGURED_PROJECT, "Projects");
        return List.of(filterByProject, projectNamePattern, configuredProject);
    }
}

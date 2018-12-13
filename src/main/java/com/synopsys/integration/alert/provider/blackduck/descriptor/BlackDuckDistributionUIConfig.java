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
package com.synopsys.integration.alert.provider.blackduck.descriptor;

import java.util.Arrays;
import java.util.Collections;
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
    public static final String KEY_FILTER_BY_PROJECT = "channel.common.filter.by.project";
    public static final String KEY_PROJECT_NAME_PATTERN = "channel.common.project.name.pattern";
    public static final String KEY_CONFIGURED_PROJECT = "channel.common.configured.project";

    @Override
    public UIComponent generateUIComponent() {
        return new UIComponent("Black Duck", "blackduck", BlackDuckProvider.COMPONENT_NAME, "laptop", Collections.emptyList());
    }

    public List<ConfigField> createFields() {
        final ConfigField filterByProject = new CheckboxConfigField(KEY_FILTER_BY_PROJECT, "Filter by project", false, false);
        final ConfigField projectNamePattern = new TextInputConfigField(KEY_PROJECT_NAME_PATTERN, "Project name pattern", false, false);

        // TODO figure out how to create a project listing (Perhaps a new field type called table)
        final ConfigField configuredProject = new SelectConfigField(KEY_CONFIGURED_PROJECT, "Projects", true, false, Collections.emptyList());
        return Arrays.asList(filterByProject, projectNamePattern, configuredProject);
    }
}

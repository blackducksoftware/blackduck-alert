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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;

@Component
public class BlackDuckDistributionUIConfig extends ProviderDistributionUIConfig {

    @Autowired
    public BlackDuckDistributionUIConfig(final BlackDuckProvider provider) {
        super(BlackDuckDescriptor.BLACKDUCK_LABEL, BlackDuckDescriptor.BLACKDUCK_URL, BlackDuckDescriptor.BLACKDUCK_ICON, provider);
    }

    @Override
    public List<ConfigField> createProviderDistributionFields() {
        final ConfigField filterByProject = CheckboxConfigField.create(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT, "Filter by project");
        final ConfigField projectNamePattern = TextInputConfigField.create(BlackDuckDescriptor.KEY_PROJECT_NAME_PATTERN, "Project name pattern", this::validateProjectNamePattern);

        // TODO figure out how to create a project listing (Perhaps a new field type called table)
        // TODO create a linkedField that is an endpoint the UI hits to generate a field
        final ConfigField configuredProject = SelectConfigField.createRequiredEmpty(BlackDuckDescriptor.KEY_CONFIGURED_PROJECT, "Projects", this::validateConfiguredProject);
        return List.of(filterByProject, projectNamePattern, configuredProject);
    }

    private Collection<String> validateProjectNamePattern(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final String projectNamePattern = fieldToValidate.getValue().orElse(null);
        if (StringUtils.isNotBlank(projectNamePattern)) {
            try {
                Pattern.compile(projectNamePattern);
            } catch (final PatternSyntaxException e) {
                return List.of("Project name pattern is not a regular expression. " + e.getMessage());
            }
        }
        return List.of();
    }

    private Collection<String> validateConfiguredProject(final FieldValueModel fieldToValidate, final FieldModel fieldModel) {
        final Collection<String> configuredProjects = Optional.ofNullable(fieldToValidate.getValues()).orElse(List.of());
        final boolean filterByProject = fieldModel.getField(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT).flatMap(FieldValueModel::getValue).map(Boolean::parseBoolean).orElse(false);
        final String projectNamePattern = fieldModel.getField(BlackDuckDescriptor.KEY_PROJECT_NAME_PATTERN).flatMap(FieldValueModel::getValue).orElse(null);
        final boolean missingProject = (null == configuredProjects || configuredProjects.isEmpty()) && StringUtils.isBlank(projectNamePattern);
        if (filterByProject && missingProject) {
            return List.of("You must select at least one project.");
        }
        return List.of();
    }
}

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

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.ProviderDistributionDescriptorActionApi;

@Component
public class BlackDuckDistributionDescriptorActionApi extends ProviderDistributionDescriptorActionApi {
    private final ContentConverter contentConverter;

    @Autowired
    public BlackDuckDistributionDescriptorActionApi(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public void validateProviderDistributionConfig(final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        final String filterByProject = fieldAccessor.getString(BlackDuckDistributionUIConfig.KEY_FILTER_BY_PROJECT).orElse(null);
        if (StringUtils.isNotBlank(filterByProject) && !contentConverter.isBoolean(filterByProject)) {
            fieldErrors.put(BlackDuckDistributionUIConfig.KEY_FILTER_BY_PROJECT, "Not a Boolean.");
        }
        final String projectNamePattern = fieldAccessor.getString(BlackDuckDistributionUIConfig.KEY_PROJECT_NAME_PATTERN).orElse(null);
        if (StringUtils.isNotBlank(projectNamePattern)) {
            try {
                Pattern.compile(projectNamePattern);
            } catch (final PatternSyntaxException e) {
                fieldErrors.put(BlackDuckDistributionUIConfig.KEY_PROJECT_NAME_PATTERN, "Project name pattern is not a regular expression. " + e.getMessage());
            }
        }
        final Collection<String> configuredProjects = fieldAccessor.getAllStrings(BlackDuckDistributionUIConfig.KEY_CONFIGURED_PROJECT);
        if (contentConverter.getBooleanValue(filterByProject) && (null == configuredProjects || configuredProjects.isEmpty()) && StringUtils.isBlank(projectNamePattern)) {
            fieldErrors.put(BlackDuckDistributionUIConfig.KEY_CONFIGURED_PROJECT, "You must select at least one project.");
        }
    }

    // TODO Add the Delete/update/save overrides here to customize what Blackduck does in each scenario(Modifying projects)
    // TODO Create a ProviderDescriptorActionApi which updates NotificationTypes in the Table (If we decide to keep this table)
}

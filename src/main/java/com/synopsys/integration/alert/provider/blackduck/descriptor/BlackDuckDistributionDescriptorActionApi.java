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
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.context.ProviderDistributionDescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.web.model.FieldModel;

@Component
public class BlackDuckDistributionDescriptorActionApi extends ProviderDistributionDescriptorActionApi {
    private final ContentConverter contentConverter;

    @Autowired
    public BlackDuckDistributionDescriptorActionApi(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public void validateConfig(final Collection<ConfigField> descriptorFields, final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        super.validateConfig(descriptorFields, fieldModel, fieldErrors);

        final String filterByProject = fieldModel.getField(BlackDuckDescriptor.KEY_FILTER_BY_PROJECT).flatMap(field -> field.getValue()).orElse(null);
        final String projectNamePattern = fieldModel.getField(BlackDuckDescriptor.KEY_PROJECT_NAME_PATTERN).flatMap(field -> field.getValue()).orElse(null);
        final Collection<String> configuredProjects = fieldModel.getField(BlackDuckDescriptor.KEY_CONFIGURED_PROJECT).flatMap(field -> Optional.ofNullable(field.getValues())).orElse(List.of());
        if (contentConverter.getBooleanValue(filterByProject) && (null == configuredProjects || configuredProjects.isEmpty()) && StringUtils.isBlank(projectNamePattern)) {
            fieldErrors.put(BlackDuckDescriptor.KEY_CONFIGURED_PROJECT, "You must select at least one project.");
        }
    }

    // TODO Add the Delete/update/save overrides here to customize what Blackduck does in each scenario(Modifying projects)
    // TODO Create a ProviderDescriptorActionApi which updates NotificationTypes in the Table (If we decide to keep this table)
}

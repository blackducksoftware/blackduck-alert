/**
 * blackduck-alert
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
package com.synopsys.integration.alert.provider.blackduck.filter;

import java.util.Collection;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.workflow.filter.notification.ProviderNotificationFilter;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDistributionUIConfig;
import com.synopsys.integration.alert.provider.blackduck.filter.field.BlackDuckNotificationFieldWrapper;

@Component
public class BlackDuckFilter implements ProviderNotificationFilter<BlackDuckNotificationFieldWrapper> {
    @Override
    public boolean doesNotificationApplyToConfiguration(BlackDuckNotificationFieldWrapper notification, ConfigurationJobModel configurationJobModel) {
        FieldAccessor fieldAccessor = configurationJobModel.getFieldAccessor();
        Boolean filterByProject = fieldAccessor.getBooleanOrFalse(BlackDuckDistributionUIConfig.KEY_FILTER_BY_PROJECT);
        if (filterByProject) {
            Collection<String> configuredProjects = fieldAccessor.getAllStrings(BlackDuckDistributionUIConfig.KEY_CONFIGURED_PROJECT);
            String nullablePattern = fieldAccessor.getStringOrNull(BlackDuckDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
            return doProjectsFromNotificationMatchConfiguredProjects(notification, configuredProjects, nullablePattern);
        }
        return true;
    }

    private boolean doProjectsFromNotificationMatchConfiguredProjects(BlackDuckNotificationFieldWrapper notification, Collection<String> configuredProjects, @Nullable String nullablePattern) {
        for (String notificationProjectName : notification.getProjectNames()) {
            if (configuredProjects.contains(notificationProjectName)) {
                return true;
            } else if (null != nullablePattern && notificationProjectName.matches(nullablePattern)) {
                return true;
            }
        }
        return false;
    }

}

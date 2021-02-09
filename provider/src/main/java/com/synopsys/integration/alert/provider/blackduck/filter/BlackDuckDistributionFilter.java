/*
 * provider
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
package com.synopsys.integration.alert.provider.blackduck.filter;

import java.util.Collection;

import org.springframework.lang.Nullable;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class BlackDuckDistributionFilter implements ProviderDistributionFilter {
    private BlackDuckProjectNameExtractor blackDuckProjectNameExtractor;
    private NotificationDeserializationCache cache;

    public BlackDuckDistributionFilter(Gson gson, ProviderNotificationClassMap providerNotificationClassMap, BlackDuckProjectNameExtractor blackDuckProjectNameExtractor) {
        this.cache = new NotificationDeserializationCache(gson, providerNotificationClassMap);
        this.blackDuckProjectNameExtractor = blackDuckProjectNameExtractor;
    }

    @Override
    public boolean doesNotificationApplyToConfiguration(AlertNotificationModel notification, ConfigurationJobModel configurationJobModel) {
        if (NotificationType.LICENSE_LIMIT.name().equals(notification.getNotificationType())) {
            // License Limit notifications are always allowed because they do not have projects.
            return true;
        }

        FieldUtility fieldUtility = configurationJobModel.getFieldUtility();
        boolean filterByProject = fieldUtility.getBooleanOrFalse(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT);
        if (filterByProject) {
            Collection<String> configuredProjects = fieldUtility.getAllStrings(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT);
            String nullablePattern = fieldUtility.getStringOrNull(ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN);
            return doProjectsFromNotificationMatchConfiguredProjects(notification, configuredProjects, nullablePattern);
        }
        return true;
    }

    @Override
    public NotificationDeserializationCache getCache() {
        return cache;
    }

    private boolean doProjectsFromNotificationMatchConfiguredProjects(AlertNotificationModel notification, Collection<String> configuredProjects, @Nullable String nullablePattern) {
        Collection<String> notificationProjectNames = blackDuckProjectNameExtractor.getProjectNames(getCache(), notification);
        for (String notificationProjectName : notificationProjectNames) {
            if (configuredProjects.contains(notificationProjectName) || (null != nullablePattern && notificationProjectName.matches(nullablePattern))) {
                return true;
            }
        }
        return false;
    }

}

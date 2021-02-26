/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.filter;

import java.util.Collection;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.provider.notification.ProviderDistributionFilter;
import com.synopsys.integration.alert.common.provider.notification.ProviderNotificationClassMap;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class BlackDuckDistributionFilter implements ProviderDistributionFilter {
    private final BlackDuckProjectNameExtractor blackDuckProjectNameExtractor;
    private final NotificationDeserializationCache cache;

    public BlackDuckDistributionFilter(Gson gson, ProviderNotificationClassMap providerNotificationClassMap, BlackDuckProjectNameExtractor blackDuckProjectNameExtractor) {
        this.cache = new NotificationDeserializationCache(gson, providerNotificationClassMap);
        this.blackDuckProjectNameExtractor = blackDuckProjectNameExtractor;
    }

    @Override
    public boolean doesNotificationApplyToConfiguration(AlertNotificationModel notification, DistributionJobModel distributionJob) {
        if (NotificationType.LICENSE_LIMIT.name().equals(notification.getNotificationType())) {
            // License Limit notifications are always allowed because they do not have projects.
            return true;
        }

        boolean filterByProject = distributionJob.isFilterByProject();
        if (filterByProject) {
            // TODO consider filtering by href
            Collection<String> configuredProjects = distributionJob.getProjectFilterDetails()
                                                        .stream()
                                                        .map(BlackDuckProjectDetailsModel::getName)
                                                        .collect(Collectors.toSet());
            String nullablePattern = distributionJob.getProjectNamePattern().orElse(null);
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

/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;

import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

public class JobNotificationFilterUtils {

    public static boolean doesNotificationApplyToJob(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, DetailedNotificationContent detailedNotificationContent) {
        String notificationType = detailedNotificationContent.getNotificationContentWrapper().extractNotificationType();
        if (!doesNotificationTypeMatch(filteredDistributionJobResponseModel, notificationType)) {
            return false;
        }
        String projectName = detailedNotificationContent.getProjectName().orElse("");
        if (!doesProjectApplyToJob(filteredDistributionJobResponseModel, projectName)) {
            return false;
        }
        List<String> notificationSeverities = detailedNotificationContent.getVulnerabilitySeverities();
        if (!doVulnerabilitySeveritiesApplyToJob(filteredDistributionJobResponseModel, notificationType, notificationSeverities)) {
            return false;
        }
        String policyName = detailedNotificationContent.getPolicyName().orElse("");
        return doesPolicyApplyToJob(filteredDistributionJobResponseModel, notificationType, policyName);
    }

    public static boolean doesNotificationTypeMatch(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, String notificationType) {
        return filteredDistributionJobResponseModel.getNotificationTypes().contains(notificationType);
    }

    public static boolean doesProjectApplyToJob(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, String projectName) {
        if (!filteredDistributionJobResponseModel.isFilterByProject()) {
            return true;
        }

        String projectNamePattern = filteredDistributionJobResponseModel.getProjectNamePattern();
        if (projectNamePattern != null && Pattern.matches(projectNamePattern, projectName)) {
            return true;
        }

        return filteredDistributionJobResponseModel.getProjectDetails()
                   .stream()
                   .map(BlackDuckProjectDetailsModel::getName)
                   .distinct()
                   .anyMatch(projectName::equals);
    }

    public static boolean doVulnerabilitySeveritiesApplyToJob(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, String notificationType, List<String> notificationSeverities) {
        if (NotificationType.VULNERABILITY.name().equals(notificationType)) {
            List<String> jobVulnerabilitySeverityFilters = filteredDistributionJobResponseModel.getVulnerabilitySeverityNames();
            if (jobVulnerabilitySeverityFilters.isEmpty()) {
                return true;
            }
            return CollectionUtils.containsAny(notificationSeverities, jobVulnerabilitySeverityFilters);
        }
        return true;
    }

    public static boolean doesPolicyApplyToJob(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, String notificationType, String policyName) {
        if (NotificationType.POLICY_OVERRIDE.name().equals(notificationType)) {
            List<String> policyNamesFilters = filteredDistributionJobResponseModel.getPolicyNames();
            if (policyNamesFilters.isEmpty()) {
                return true;
            }
            return policyNamesFilters.contains(policyName);
        }
        return true;
    }

    private JobNotificationFilterUtils() {
        //this class should not be instantiated
    }
}

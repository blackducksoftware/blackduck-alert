/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.filter;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.api.processor.detail.DetailedNotificationContent;
import com.blackduck.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.blackduck.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.blackduck.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

public class JobNotificationFilterUtils {
    public static boolean doesNotificationApplyToJob(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, DetailedNotificationContent detailedNotificationContent) {
        String notificationType = detailedNotificationContent.getNotificationContentWrapper().extractNotificationType();
        NotificationType notificationTypeEnum = NotificationType.valueOf(notificationType);

        if (!doesNotificationTypeMatch(filteredDistributionJobResponseModel, notificationType)) {
            return false;
        }
        String projectName = detailedNotificationContent.getProjectName().orElse("");
        String projectVersionName = detailedNotificationContent.getProjectVersionName().orElse("");
        if (!doesProjectApplyToJob(filteredDistributionJobResponseModel, projectName, projectVersionName)) {
            return false;
        }
        switch (notificationTypeEnum) {
            case VULNERABILITY:
                List<String> notificationSeverities = detailedNotificationContent.getVulnerabilitySeverities();
                return doVulnerabilitySeveritiesApplyToJob(filteredDistributionJobResponseModel, notificationSeverities);
            case POLICY_OVERRIDE:
            case RULE_VIOLATION:
            case RULE_VIOLATION_CLEARED:
                String policyName = detailedNotificationContent.getPolicyName().orElse("");
                return doesPolicyApplyToJob(filteredDistributionJobResponseModel, policyName);
            default:
                break;
        }
        return true;
    }

    public static boolean doesNotificationTypeMatch(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, String notificationType) {
        return filteredDistributionJobResponseModel.getNotificationTypes().contains(notificationType);
    }

    public static boolean doesProjectApplyToJob(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, String projectName, String projectVersionName) {
        if (!filteredDistributionJobResponseModel.isFilterByProject()) {
            return true;
        }

        String projectNamePattern = filteredDistributionJobResponseModel.getProjectNamePattern();
        boolean matchingProjectNamePattern = StringUtils.isNotBlank(projectNamePattern) && Pattern.matches(projectNamePattern, projectName);
        boolean hasMatchingProjects = filteredDistributionJobResponseModel.getProjectDetails()
            .stream()
            .map(BlackDuckProjectDetailsModel::getName)
            .distinct()
            .anyMatch(projectName::equals);

        String projectVersionNamePattern = filteredDistributionJobResponseModel.getProjectVersionNamePattern();
        if (StringUtils.isNotBlank(projectVersionNamePattern)) {
            // Project version pattern has to always be valid and if something else exists (Selected project or project name pattern), that also needs to be valid
            boolean selectedProjectsOrNamePatternMatches = hasMatchingProjects || matchingProjectNamePattern;
            if (!selectedProjectsOrNamePatternMatches && StringUtils.isNotBlank(projectNamePattern)) {
                return false;
            }
            boolean noSelectedProjects = filteredDistributionJobResponseModel.getProjectDetails().isEmpty();
            boolean projectMatchedOrNoneSelected = noSelectedProjects || selectedProjectsOrNamePatternMatches;
            return projectMatchedOrNoneSelected && Pattern.matches(projectVersionNamePattern, projectVersionName);
        }

        return matchingProjectNamePattern || hasMatchingProjects;
    }

    public static boolean doesProjectApplyToJob(SimpleFilteredDistributionJobResponseModel filteredDistributionJobResponseModel, String projectName, String projectVersionName) {
        // not filtering by project so all projects apply.
        if (!filteredDistributionJobResponseModel.isFilterByProject()) {
            return true;
        }
        String projectNamePattern = filteredDistributionJobResponseModel.getProjectNamePattern();
        String projectVersionNamePattern = filteredDistributionJobResponseModel.getProjectVersionNamePattern();

        boolean matchingProjectNamePattern = StringUtils.isNotBlank(projectNamePattern) && Pattern.matches(projectNamePattern, projectName);
        boolean hasMatchingProjects = filteredDistributionJobResponseModel.hasProjectsConfigured();
        if (StringUtils.isNotBlank(projectVersionNamePattern)) {
            // Project version pattern has to always be valid and if something else exists (Selected project or project name pattern), that also needs to be valid
            boolean selectedProjectsOrNamePatternMatches = hasMatchingProjects || matchingProjectNamePattern;
            if (!selectedProjectsOrNamePatternMatches && StringUtils.isNotBlank(projectNamePattern)) {
                return false;
            }
            // match version pattern because project pattern matches or the project was selected
            return Pattern.matches(projectVersionNamePattern, projectVersionName);
        }

        return matchingProjectNamePattern || hasMatchingProjects;
    }

    public static boolean doVulnerabilitySeveritiesApplyToJob(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, List<String> notificationSeverities) {
        List<String> jobVulnerabilitySeverityFilters = filteredDistributionJobResponseModel.getVulnerabilitySeverityNames();
        if (jobVulnerabilitySeverityFilters.isEmpty()) {
            return true;
        }
        return CollectionUtils.containsAny(notificationSeverities, jobVulnerabilitySeverityFilters);
    }

    public static boolean doesPolicyApplyToJob(FilteredDistributionJobResponseModel filteredDistributionJobResponseModel, String policyName) {
        List<String> policyNamesFilters = filteredDistributionJobResponseModel.getPolicyNames();
        if (policyNamesFilters.isEmpty()) {
            return true;
        }
        return policyNamesFilters.contains(policyName);
    }

    private JobNotificationFilterUtils() {
        //this class should not be instantiated
    }
}

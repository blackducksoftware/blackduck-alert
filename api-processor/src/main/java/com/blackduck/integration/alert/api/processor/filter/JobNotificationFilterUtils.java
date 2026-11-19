/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.filter;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blackduck.integration.alert.common.persistence.model.job.SimpleFilteredDistributionJobResponseModel;

public class JobNotificationFilterUtils {

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

    private JobNotificationFilterUtils() {
        //this class should not be instantiated
    }
}

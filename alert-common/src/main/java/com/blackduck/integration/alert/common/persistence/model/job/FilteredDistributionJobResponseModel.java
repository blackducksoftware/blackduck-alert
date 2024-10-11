/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job;

import java.util.List;
import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;

public class FilteredDistributionJobResponseModel extends AlertSerializableModel {
    private final UUID id;
    private final ProcessingType processingType;
    private final String channelName;
    private final String jobName;
    private final List<String> notificationTypes;
    private final List<BlackDuckProjectDetailsModel> projectDetails;
    private final List<String> policyNames;
    private final List<String> vulnerabilitySeverityNames;
    private final boolean filterByProject;
    private final String projectNamePattern;
    private final String projectVersionNamePattern;

    public FilteredDistributionJobResponseModel(
        UUID id,
        ProcessingType processingType,
        String channelName,
        String jobName,
        List<String> notificationTypes,
        List<BlackDuckProjectDetailsModel> projectDetails,
        List<String> policyNames,
        List<String> vulnerabilitySeverityNames,
        boolean filterByProject,
        String projectNamePattern,
        String projectVersionNamePattern
    ) {
        this.processingType = processingType;
        this.id = id;
        this.channelName = channelName;
        this.jobName = jobName;
        this.notificationTypes = notificationTypes;
        this.projectDetails = projectDetails;
        this.policyNames = policyNames;
        this.vulnerabilitySeverityNames = vulnerabilitySeverityNames;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
        this.projectVersionNamePattern = projectVersionNamePattern;
    }

    public UUID getId() {
        return id;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getJobName() {
        return jobName;
    }

    public List<String> getNotificationTypes() {
        return notificationTypes;
    }

    public List<BlackDuckProjectDetailsModel> getProjectDetails() {
        return projectDetails;
    }

    public List<String> getPolicyNames() {
        return policyNames;
    }

    public List<String> getVulnerabilitySeverityNames() {
        return vulnerabilitySeverityNames;
    }

    public boolean isFilterByProject() {
        return filterByProject;
    }

    public String getProjectNamePattern() {
        return projectNamePattern;
    }

    public String getProjectVersionNamePattern() {
        return projectVersionNamePattern;
    }
}

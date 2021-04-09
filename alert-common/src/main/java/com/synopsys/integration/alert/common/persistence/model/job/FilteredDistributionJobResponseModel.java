/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class FilteredDistributionJobResponseModel extends AlertSerializableModel {
    private final UUID id;
    private final ProcessingType processingType;
    private final String channelName;
    private final List<String> notificationTypes;
    private final List<BlackDuckProjectDetailsModel> projectDetails;
    private final List<String> policyNames;
    private final List<String> vulnerabilitySeverityNames;
    private final boolean filterByProject;
    private final String projectNamePattern;

    public FilteredDistributionJobResponseModel(
        UUID id,
        ProcessingType processingType,
        String channelName,
        List<String> notificationTypes,
        List<BlackDuckProjectDetailsModel> projectDetails,
        List<String> policyNames,
        List<String> vulnerabilitySeverityNames,
        boolean filterByProject,
        String projectNamePattern
    ) {
        this.processingType = processingType;
        this.id = id;
        this.channelName = channelName;
        this.notificationTypes = notificationTypes;
        this.projectDetails = projectDetails;
        this.policyNames = policyNames;
        this.vulnerabilitySeverityNames = vulnerabilitySeverityNames;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
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
}

/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public abstract class DistributionJobModelData extends AlertSerializableModel {
    private final boolean enabled;
    private final String name;
    private final FrequencyType distributionFrequency;
    private final ProcessingType processingType;
    private final String channelDescriptorName;

    // Black Duck fields will be common as long as it is the only provider
    private final Long blackDuckGlobalConfigId;
    private final boolean filterByProject;
    @Nullable
    private final String projectNamePattern;
    private final String projectVersionNamePattern;
    private final List<String> notificationTypes;
    private final List<BlackDuckProjectDetailsModel> projectFilterDetails;
    private final List<String> policyFilterPolicyNames;
    private final List<String> vulnerabilityFilterSeverityNames;

    private final DistributionJobDetailsModel distributionJobDetails;

    /* package private */ DistributionJobModelData() {
        this.enabled = true;
        this.name = null;
        this.distributionFrequency = FrequencyType.REAL_TIME;
        this.processingType = ProcessingType.DEFAULT;
        this.channelDescriptorName = null;
        this.blackDuckGlobalConfigId = null;
        this.filterByProject = false;
        this.projectNamePattern = null;
        this.projectVersionNamePattern = null;
        this.notificationTypes = List.of();
        this.projectFilterDetails = List.of();
        this.policyFilterPolicyNames = List.of();
        this.vulnerabilityFilterSeverityNames = List.of();
        this.distributionJobDetails = null;
    }

    public DistributionJobModelData(
        boolean enabled,
        String name,
        FrequencyType distributionFrequency,
        ProcessingType processingType,
        String channelDescriptorName,
        Long blackDuckGlobalConfigId,
        boolean filterByProject,
        @Nullable String projectNamePattern,
        @Nullable String projectVersionNamePattern,
        List<String> notificationTypes,
        List<BlackDuckProjectDetailsModel> projectFilterDetails,
        List<String> policyFilterPolicyNames,
        List<String> vulnerabilityFilterSeverityNames,
        DistributionJobDetailsModel distributionJobDetails
    ) {
        this.enabled = enabled;
        this.name = name;
        this.distributionFrequency = distributionFrequency;
        this.processingType = processingType;
        this.channelDescriptorName = channelDescriptorName;
        this.blackDuckGlobalConfigId = blackDuckGlobalConfigId;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
        this.projectVersionNamePattern = projectVersionNamePattern;
        this.notificationTypes = notificationTypes;
        this.projectFilterDetails = projectFilterDetails;
        this.policyFilterPolicyNames = policyFilterPolicyNames;
        this.vulnerabilityFilterSeverityNames = vulnerabilityFilterSeverityNames;
        this.distributionJobDetails = distributionJobDetails;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public FrequencyType getDistributionFrequency() {
        return distributionFrequency;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public String getChannelDescriptorName() {
        return channelDescriptorName;
    }

    public Long getBlackDuckGlobalConfigId() {
        return blackDuckGlobalConfigId;
    }

    public boolean isFilterByProject() {
        return filterByProject;
    }

    public Optional<String> getProjectNamePattern() {
        return Optional.ofNullable(projectNamePattern);
    }

    public Optional<String> getProjectVersionNamePattern() {
        return Optional.ofNullable(projectVersionNamePattern);
    }

    public List<String> getNotificationTypes() {
        return notificationTypes;
    }

    public List<BlackDuckProjectDetailsModel> getProjectFilterDetails() {
        return projectFilterDetails;
    }

    public List<String> getPolicyFilterPolicyNames() {
        return policyFilterPolicyNames;
    }

    public List<String> getVulnerabilityFilterSeverityNames() {
        return vulnerabilityFilterSeverityNames;
    }

    public DistributionJobDetailsModel getDistributionJobDetails() {
        return distributionJobDetails;
    }

}

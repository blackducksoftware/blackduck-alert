package com.synopsys.integration.alert.common.persistence.model.job;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class DistributionJobModelBuilder {
    private UUID jobId;
    private boolean enabled = true;
    private String name;
    private String distributionFrequency;
    private String processingType;
    private String channelDescriptorName;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastUpdated;

    private Long blackDuckGlobalConfigId;
    private boolean filterByProject = false;
    private String projectNamePattern;
    private List<String> notificationTypes;
    private List<String> policyFilterPolicyNames;
    private List<String> vulnerabilityFilterSeverityNames;

    public DistributionJobModelBuilder() {
    }

    public DistributionJobModel build() {
        return new DistributionJobModel(
            jobId,
            enabled,
            name,
            distributionFrequency,
            processingType,
            channelDescriptorName,
            createdAt,
            lastUpdated,
            blackDuckGlobalConfigId,
            filterByProject,
            projectNamePattern,
            notificationTypes,
            policyFilterPolicyNames,
            vulnerabilityFilterSeverityNames
        );
    }

    public DistributionJobModelBuilder jobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    public DistributionJobModelBuilder enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public DistributionJobModelBuilder name(String name) {
        this.name = name;
        return this;
    }

    public DistributionJobModelBuilder distributionFrequency(String distributionFrequency) {
        this.distributionFrequency = distributionFrequency;
        return this;
    }

    public DistributionJobModelBuilder processingType(String processingType) {
        this.processingType = processingType;
        return this;
    }

    public DistributionJobModelBuilder channelDescriptorName(String channelDescriptorName) {
        this.channelDescriptorName = channelDescriptorName;
        return this;
    }

    public DistributionJobModelBuilder createdAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public DistributionJobModelBuilder lastUpdated(OffsetDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public DistributionJobModelBuilder blackDuckGlobalConfigId(Long blackDuckGlobalConfigId) {
        this.blackDuckGlobalConfigId = blackDuckGlobalConfigId;
        return this;
    }

    public DistributionJobModelBuilder filterByProject(boolean filterByProject) {
        this.filterByProject = filterByProject;
        return this;
    }

    public DistributionJobModelBuilder projectNamePattern(String projectNamePattern) {
        this.projectNamePattern = projectNamePattern;
        return this;
    }

    public DistributionJobModelBuilder notificationTypes(List<String> notificationTypes) {
        this.notificationTypes = notificationTypes;
        return this;
    }

    public DistributionJobModelBuilder policyFilterPolicyNames(List<String> policyFilterPolicyNames) {
        this.policyFilterPolicyNames = policyFilterPolicyNames;
        return this;
    }

    public DistributionJobModelBuilder vulnerabilityFilterSeverityNames(List<String> vulnerabilityFilterSeverityNames) {
        this.vulnerabilityFilterSeverityNames = vulnerabilityFilterSeverityNames;
        return this;
    }

}

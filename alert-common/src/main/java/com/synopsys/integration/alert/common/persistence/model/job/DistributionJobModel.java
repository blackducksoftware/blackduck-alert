package com.synopsys.integration.alert.common.persistence.model.job;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class DistributionJobModel extends AlertSerializableModel {
    private final UUID jobId;
    private final boolean enabled;
    private final String name;
    private final String distributionFrequency;
    private final String processingType;
    private final String channelDescriptorName;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime lastUpdated;

    // Black Duck fields will be common as long as it is the only provider
    private final Long blackDuckGlobalConfigId;
    private final boolean filterByProject;
    private final String projectNamePattern;
    private final List<String> notificationTypes;
    private final List<String> projectFilterProjectNames;
    private final List<String> policyFilterPolicyNames;
    private final List<String> vulnerabilityFilterSeverityNames;

    public static DistributionJobModelBuilder builder() {
        return new DistributionJobModelBuilder();
    }

    protected DistributionJobModel(
        UUID jobId,
        boolean enabled,
        String name,
        String distributionFrequency,
        String processingType,
        String channelDescriptorName,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        Long blackDuckGlobalConfigId,
        boolean filterByProject,
        String projectNamePattern,
        List<String> notificationTypes,
        List<String> projectFilterProjectNames, List<String> policyFilterPolicyNames,
        List<String> vulnerabilityFilterSeverityNames
    ) {
        this.jobId = jobId;
        this.enabled = enabled;
        this.name = name;
        this.distributionFrequency = distributionFrequency;
        this.processingType = processingType;
        this.channelDescriptorName = channelDescriptorName;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.blackDuckGlobalConfigId = blackDuckGlobalConfigId;
        this.filterByProject = filterByProject;
        this.projectNamePattern = projectNamePattern;
        this.notificationTypes = notificationTypes;
        this.projectFilterProjectNames = projectFilterProjectNames;
        this.policyFilterPolicyNames = policyFilterPolicyNames;
        this.vulnerabilityFilterSeverityNames = vulnerabilityFilterSeverityNames;
    }

    @Nullable
    public UUID getJobId() {
        return jobId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    public String getDistributionFrequency() {
        return distributionFrequency;
    }

    public String getProcessingType() {
        return processingType;
    }

    public String getChannelDescriptorName() {
        return channelDescriptorName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Nullable
    public OffsetDateTime getLastUpdated() {
        return lastUpdated;
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

    public List<String> getNotificationTypes() {
        return notificationTypes;
    }

    public List<String> getProjectFilterProjectNames() {
        return projectFilterProjectNames;
    }

    public List<String> getPolicyFilterPolicyNames() {
        return policyFilterPolicyNames;
    }

    public List<String> getVulnerabilityFilterSeverityNames() {
        return vulnerabilityFilterSeverityNames;
    }

}

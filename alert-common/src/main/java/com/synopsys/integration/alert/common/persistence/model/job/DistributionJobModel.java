/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public class DistributionJobModel extends DistributionJobModelData {
    private final UUID jobId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime lastUpdated;

    public static DistributionJobModelBuilder builder() {
        return new DistributionJobModelBuilder();
    }

    /* package private */ DistributionJobModel() {
        this.jobId = null;
        this.createdAt = null;
        this.lastUpdated = null;
    }

    protected DistributionJobModel(
        UUID jobId,
        boolean enabled,
        String name,
        FrequencyType distributionFrequency,
        ProcessingType processingType,
        String channelDescriptorName,
        OffsetDateTime createdAt,
        OffsetDateTime lastUpdated,
        Long blackDuckGlobalConfigId,
        boolean filterByProject,
        String projectNamePattern,
        String projectVersionNamePattern,
        List<String> notificationTypes,
        List<BlackDuckProjectDetailsModel> projectFilterDetails,
        List<String> policyFilterPolicyNames,
        List<String> vulnerabilityFilterSeverityNames,
        DistributionJobDetailsModel distributionJobDetails
    ) {
        super(
            enabled,
            name,
            distributionFrequency,
            processingType,
            channelDescriptorName,
            blackDuckGlobalConfigId,
            filterByProject,
            projectNamePattern,
            projectVersionNamePattern,
            notificationTypes,
            projectFilterDetails,
            policyFilterPolicyNames,
            vulnerabilityFilterSeverityNames,
            distributionJobDetails
        );
        this.jobId = jobId;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
    }

    @Nullable
    public UUID getJobId() {
        return jobId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Optional<OffsetDateTime> getLastUpdated() {
        return Optional.ofNullable(lastUpdated);
    }

}

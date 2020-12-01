/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        List<String> notificationTypes,
        List<String> projectFilterProjectNames,
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
            notificationTypes,
            projectFilterProjectNames,
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

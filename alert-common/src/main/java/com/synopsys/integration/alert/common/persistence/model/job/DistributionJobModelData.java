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

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

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
    private final List<String> notificationTypes;
    private final List<String> projectFilterProjectNames;
    private final List<String> policyFilterPolicyNames;
    private final List<String> vulnerabilityFilterSeverityNames;

    private final DistributionJobDetailsModel distributionJobDetails;

    public DistributionJobModelData(
        boolean enabled,
        String name,
        FrequencyType distributionFrequency,
        ProcessingType processingType,
        String channelDescriptorName,
        Long blackDuckGlobalConfigId,
        boolean filterByProject,
        @Nullable String projectNamePattern,
        List<String> notificationTypes,
        List<String> projectFilterProjectNames,
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
        this.notificationTypes = notificationTypes;
        this.projectFilterProjectNames = projectFilterProjectNames;
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

    public DistributionJobDetailsModel getDistributionJobDetails() {
        return distributionJobDetails;
    }
}

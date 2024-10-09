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

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.common.enumeration.FrequencyType;
import com.blackduck.integration.alert.common.enumeration.ProcessingType;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public class DistributionJobRequestModel extends DistributionJobModelData {
    public DistributionJobRequestModel(
        boolean enabled,
        String name,
        FrequencyType distributionFrequency,
        ProcessingType processingType,
        String channelDescriptorName,
        UUID channelGlobalConfigId,
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
        super(enabled, name, distributionFrequency, processingType, channelDescriptorName, channelGlobalConfigId, blackDuckGlobalConfigId, filterByProject, projectNamePattern, projectVersionNamePattern, notificationTypes,
            projectFilterDetails,
            policyFilterPolicyNames, vulnerabilityFilterSeverityNames, distributionJobDetails);
    }

}

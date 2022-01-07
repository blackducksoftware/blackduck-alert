/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public class DistributionJobRequestModel extends DistributionJobModelData {
    public DistributionJobRequestModel(
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
        super(enabled, name, distributionFrequency, processingType, channelDescriptorName, blackDuckGlobalConfigId, filterByProject, projectNamePattern, projectVersionNamePattern, notificationTypes, projectFilterDetails,
            policyFilterPolicyNames, vulnerabilityFilterSeverityNames, distributionJobDetails);
    }

}

/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.details.processor;

import java.util.Map;
import java.util.UUID;

import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.descriptor.action.DescriptorAction;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public abstract class DistributionJobDetailsExtractor extends DescriptorAction {
    protected DistributionJobDetailsExtractor(DescriptorKey descriptorKey) {
        super(descriptorKey);
    }

    public abstract DistributionJobDetailsModel extractDetails(UUID jobId, Map<String, ConfigurationFieldModel> configuredFieldsMap);

}

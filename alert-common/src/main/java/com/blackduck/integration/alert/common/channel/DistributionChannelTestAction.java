/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.channel;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.descriptor.action.DescriptorAction;
import com.blackduck.integration.alert.common.message.model.MessageResult;
import com.blackduck.integration.alert.common.persistence.model.job.DistributionJobModel;

public abstract class DistributionChannelTestAction extends DescriptorAction {
    protected DistributionChannelTestAction(DescriptorKey descriptorKey) {
        super(descriptorKey);
    }

    public abstract MessageResult testConfig(
        DistributionJobModel distributionJobModel,
        String jobName,
        @Nullable String customTopic,
        @Nullable String customMessage
    ) throws AlertException;

}

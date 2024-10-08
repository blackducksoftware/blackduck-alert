/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.action.DescriptorAction;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;

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

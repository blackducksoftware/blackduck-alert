/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventReceiver;
import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.google.gson.Gson;

@Component
public class AzureBoardsDistributionEventReceiver extends DistributionEventReceiver<AzureBoardsJobDetailsModel> {
    @Autowired
    public AzureBoardsDistributionEventReceiver(
        Gson gson,
        TaskExecutor taskExecutor,
        AzureBoardsChannelKey channelKey,
        AzureBoardsDistributionEventHandler distributionEventHandler
    ) {
        super(gson, taskExecutor, channelKey, distributionEventHandler);
    }

}

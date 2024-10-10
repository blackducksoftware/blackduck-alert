/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.persistence.accessor.AzureBoardsJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class AzureBoardsDistributionEventHandler extends DistributionEventHandler<AzureBoardsJobDetailsModel> {
    @Autowired
    public AzureBoardsDistributionEventHandler(
        AzureBoardsChannel channel,
        AzureBoardsJobDetailsAccessor jobDetailsAccessor,
        EventManager eventManager
    ) {
        super(channel, jobDetailsAccessor, eventManager);
    }

}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.msteams.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventHandler;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.persistence.accessor.MSTeamsJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;

@Component
public class MSTeamsDistributionEventHandler extends DistributionEventHandler<MSTeamsJobDetailsModel> {
    @Autowired
    public MSTeamsDistributionEventHandler(MSTeamsChannel channel, MSTeamsJobDetailsAccessor jobDetailsAccessor, EventManager eventManager) {
        super(channel, jobDetailsAccessor, eventManager);
    }

}

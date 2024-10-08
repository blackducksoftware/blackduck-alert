/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.msteams.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventReceiver;
import com.google.gson.Gson;
import com.synopsys.integration.alert.api.descriptor.MsTeamsKey;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;

@Component
public class MSTeamsDistributionEventReceiver extends DistributionEventReceiver<MSTeamsJobDetailsModel> {
    @Autowired
    public MSTeamsDistributionEventReceiver(Gson gson, TaskExecutor taskExecutor, MsTeamsKey channelKey, MSTeamsDistributionEventHandler distributionEventHandler) {
        super(gson, taskExecutor, channelKey, distributionEventHandler);
    }

}

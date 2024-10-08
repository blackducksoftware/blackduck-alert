/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.msteams.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.MessageBoardChannel;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;

@Component
public class MSTeamsChannel extends MessageBoardChannel<MSTeamsJobDetailsModel, MSTeamsChannelMessageModel> {
    @Autowired
    protected MSTeamsChannel(
        MSTeamsChannelMessageConverter msTeamsChannelMessageConverter,
        MSTeamsChannelMessageSender msTeamsChannelMessageSender,
        EventManager eventManager,
        ExecutingJobManager executingJobManager
    ) {
        super(msTeamsChannelMessageConverter, msTeamsChannelMessageSender, eventManager, executingJobManager);
    }

}

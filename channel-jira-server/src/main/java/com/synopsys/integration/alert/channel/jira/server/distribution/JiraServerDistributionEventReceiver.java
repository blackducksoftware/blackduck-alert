/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.DistributionEventReceiver;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;

@Component
public class JiraServerDistributionEventReceiver extends DistributionEventReceiver<JiraServerJobDetailsModel> {
    @Autowired
    public JiraServerDistributionEventReceiver(Gson gson, TaskExecutor taskExecutor, JiraServerChannelKey channelKey, JiraServerDistributionEventHandler distributionEventHandler) {
        super(gson, taskExecutor, channelKey, distributionEventHandler);
    }

}

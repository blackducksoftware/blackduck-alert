/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.slack.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventReceiver;
import com.blackduck.integration.alert.api.descriptor.SlackChannelKey;
import com.blackduck.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.google.gson.Gson;

@Component
public class SlackDistributionEventReceiver extends DistributionEventReceiver<SlackJobDetailsModel> {
    @Autowired
    public SlackDistributionEventReceiver(Gson gson, TaskExecutor taskExecutor, SlackChannelKey channelKey, SlackDistributionEventHandler distributionEventHandler) {
        super(gson, taskExecutor, channelKey, distributionEventHandler);
    }

}

/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.DistributionEventReceiver;
import com.google.gson.Gson;
import com.blackduck.integration.alert.api.descriptor.EmailChannelKey;
import com.blackduck.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;

@Component
public class EmailDistributionEventReceiver extends DistributionEventReceiver<EmailJobDetailsModel> {
    @Autowired
    public EmailDistributionEventReceiver(Gson gson, TaskExecutor taskExecutor, EmailChannelKey channelKey, EmailDistributionEventHandler distributionEventHandler) {
        super(gson, taskExecutor, channelKey, distributionEventHandler);
    }

}

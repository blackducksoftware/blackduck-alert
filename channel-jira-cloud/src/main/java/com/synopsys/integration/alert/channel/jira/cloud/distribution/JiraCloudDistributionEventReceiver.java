/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.DistributionEventReceiver;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;

@Component
public class JiraCloudDistributionEventReceiver extends DistributionEventReceiver<JiraCloudJobDetailsModel> {
    @Autowired
    public JiraCloudDistributionEventReceiver(Gson gson, JiraCloudChannelKey channelKey, JiraCloudDistributionEventHandler distributionEventHandler) {
        super(gson, channelKey, distributionEventHandler);
    }

}

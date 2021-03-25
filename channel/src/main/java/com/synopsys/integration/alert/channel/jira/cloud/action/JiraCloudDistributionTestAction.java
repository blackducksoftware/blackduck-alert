/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.action.DistributionChannelMessageTestAction;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.JiraCloudChannel;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;

@Component
public class JiraCloudDistributionTestAction extends DistributionChannelMessageTestAction<JiraCloudJobDetailsModel> {
    @Autowired
    public JiraCloudDistributionTestAction(JiraCloudChannelKey channelKey, JiraCloudChannel distributionChannel) {
        super(channelKey, distributionChannel);
    }

}

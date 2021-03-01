/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.DistributionEventReceiver;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.SlackJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;

@Component
public class SlackDistributionEventReceiver extends DistributionEventReceiver<SlackJobDetailsModel> {
    @Autowired
    public SlackDistributionEventReceiver(Gson gson, AuditAccessor auditAccessor, SlackJobDetailsAccessor slackJobDetailsAccessor, SlackChannelV2 channel, SlackChannelKey channelKey) {
        super(gson, auditAccessor, slackJobDetailsAccessor, channel, channelKey);
    }
}

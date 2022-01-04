/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.DistributionEventReceiver;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;

@Component
public class EmailDistributionEventReceiver extends DistributionEventReceiver<EmailJobDetailsModel> {
    @Autowired
    public EmailDistributionEventReceiver(Gson gson, EmailChannelKey channelKey, EmailDistributionEventHandler distributionEventHandler) {
        super(gson, channelKey, distributionEventHandler);
    }

}

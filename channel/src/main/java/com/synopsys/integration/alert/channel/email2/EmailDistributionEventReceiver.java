/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.DistributionEventReceiver;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.EmailJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.EmailJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;

@Component
public class EmailDistributionEventReceiver extends DistributionEventReceiver<EmailJobDetailsModel> {
    @Autowired
    public EmailDistributionEventReceiver(Gson gson, AuditAccessor auditAccessor, EmailJobDetailsAccessor emailJobDetailsAccessor, EmailChannelV2 channel, EmailChannelKey channelKey) {
        super(gson, auditAccessor, emailJobDetailsAccessor, channel, channelKey);
    }

}

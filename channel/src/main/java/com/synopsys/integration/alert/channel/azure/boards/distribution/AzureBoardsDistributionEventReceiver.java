/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.DistributionEventReceiver;
import com.synopsys.integration.alert.common.descriptor.accessor.AuditAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.AzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;

@Component
public class AzureBoardsDistributionEventReceiver extends DistributionEventReceiver<AzureBoardsJobDetailsModel> {
    @Autowired
    public AzureBoardsDistributionEventReceiver(Gson gson, AuditAccessor auditAccessor, AzureBoardsJobDetailsAccessor jobDetailsAccessor, AzureBoardsChannel channel, AzureBoardsChannelKey channelKey) {
        super(gson, auditAccessor, jobDetailsAccessor, channel, channelKey);
    }

}

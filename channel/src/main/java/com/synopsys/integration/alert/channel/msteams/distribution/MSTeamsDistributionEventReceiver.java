/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.api.DistributionEventReceiver;
import com.synopsys.integration.alert.common.persistence.accessor.MSTeamsJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;

@Component
public class MSTeamsDistributionEventReceiver extends DistributionEventReceiver<MSTeamsJobDetailsModel> {
    @Autowired
    public MSTeamsDistributionEventReceiver(Gson gson, ProcessingAuditAccessor auditAccessor, MSTeamsJobDetailsAccessor msTeamsJobDetailsAccessor, MSTeamsChannel channel, MsTeamsKey channelKey) {
        super(gson, auditAccessor, msTeamsJobDetailsAccessor, channel, channelKey);
    }

}

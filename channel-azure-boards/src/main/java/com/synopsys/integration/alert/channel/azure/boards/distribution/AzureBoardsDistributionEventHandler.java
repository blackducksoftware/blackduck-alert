/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.DistributionEventHandler;
import com.synopsys.integration.alert.common.persistence.accessor.AzureBoardsJobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;
import com.synopsys.integration.alert.telemetry.database.DefaultTelemetryAccessor;

@Component
public class AzureBoardsDistributionEventHandler extends DistributionEventHandler<AzureBoardsJobDetailsModel> {
    @Autowired
    public AzureBoardsDistributionEventHandler(
        AzureBoardsChannel channel,
        AzureBoardsJobDetailsAccessor jobDetailsAccessor,
        ProcessingAuditAccessor auditAccessor,
        DefaultTelemetryAccessor telemetryAccessor
    ) {
        super(channel, jobDetailsAccessor, auditAccessor, telemetryAccessor);
    }

}

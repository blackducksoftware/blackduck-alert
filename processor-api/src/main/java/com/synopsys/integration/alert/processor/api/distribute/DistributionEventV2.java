/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.UUID;

import com.synopsys.integration.alert.common.event.AlertEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

public class DistributionEventV2 extends AlertEvent {
    private final UUID jobId;
    private final Long auditId;

    private final ProviderMessageHolder providerMessages;

    public DistributionEventV2(ChannelKey destination, UUID jobId, Long auditId, ProviderMessageHolder providerMessages) {
        super(destination.getUniversalKey());
        this.jobId = jobId;
        this.auditId = auditId;
        this.providerMessages = providerMessages;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getAuditId() {
        return auditId;
    }

    public ProviderMessageHolder getProviderMessages() {
        return providerMessages;
    }

}

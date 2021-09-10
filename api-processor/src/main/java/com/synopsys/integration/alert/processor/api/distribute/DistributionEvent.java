/*
 * api-processor
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;

public class DistributionEvent extends AlertEvent {
    private final UUID jobId;
    private final Set<Long> notificationIds;

    private final ProviderMessageHolder providerMessages;

    public DistributionEvent(ChannelKey destination, UUID jobId, Set<Long> notificationIds, ProviderMessageHolder providerMessages) {
        super(destination.getUniversalKey());
        this.jobId = jobId;
        this.notificationIds = notificationIds;
        this.providerMessages = providerMessages;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }

    public ProviderMessageHolder getProviderMessages() {
        return providerMessages;
    }

}

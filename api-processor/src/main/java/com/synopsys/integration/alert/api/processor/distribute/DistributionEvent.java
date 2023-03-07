/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.processor.distribute;

import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.api.processor.extract.model.ProviderMessageHolder;

public class DistributionEvent extends AlertEvent {
    private final UUID jobId;
    private final String jobName;
    private final Set<Long> notificationIds;

    private final UUID jobExecutionId;

    private final ProviderMessageHolder providerMessages;

    public DistributionEvent(ChannelKey destination, UUID jobId, UUID jobExecutionId, String jobName, Set<Long> notificationIds, ProviderMessageHolder providerMessages) {
        super(destination.getUniversalKey());
        this.jobId = jobId;
        this.jobName = jobName;
        this.notificationIds = notificationIds;
        this.providerMessages = providerMessages;
        this.jobExecutionId = jobExecutionId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public UUID getJobExecutionId() {
        return jobExecutionId;
    }

    public String getJobName() {
        return jobName;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }

    public ProviderMessageHolder getProviderMessages() {
        return providerMessages;
    }

}

/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.Collection;
import java.util.UUID;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public final class ProcessedNotificationDetails extends AlertSerializableModel {
    private final UUID jobId;
    private final String channelName;
    private final Collection<Long> notificationIds;

    public ProcessedNotificationDetails(UUID jobId, String channelName, Collection<Long> notificationIds) {
        this.jobId = jobId;
        this.channelName = channelName;
        this.notificationIds = notificationIds;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getChannelName() {
        return channelName;
    }

    public Collection<Long> getNotificationIds() {
        return notificationIds;
    }

}

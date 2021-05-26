/*
 * api-processor
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.distribute;

import java.util.UUID;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public final class ProcessedNotificationDetails extends AlertSerializableModel {
    private final UUID jobId;
    private final String channelName;

    public ProcessedNotificationDetails(UUID jobId, String channelName) {
        this.jobId = jobId;
        this.channelName = channelName;
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getChannelName() {
        return channelName;
    }

}

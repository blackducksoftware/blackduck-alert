package com.synopsys.integration.alert.processor.api.filter.model;

import java.util.UUID;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public final class NotificationFilterJobModel extends AlertSerializableModel {
    private final UUID jobId;
    private final String channelName;

    public NotificationFilterJobModel(UUID jobId, String channelName) {
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

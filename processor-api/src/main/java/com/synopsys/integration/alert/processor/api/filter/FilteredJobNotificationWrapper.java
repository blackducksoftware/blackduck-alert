/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

// NotificationContentWrapper is not serializable, so this class cannot be serializable (and doesn't need to be)
//FIXME: This can't extend AlertSerializableModel
//public class FilteredJobNotificationWrapper extends Stringable {
public class FilteredJobNotificationWrapper extends AlertSerializableModel {
    private final UUID jobId;
    private final ProcessingType processingType;
    private final String channelName;
    private final List<NotificationContentWrapper> jobNotifications;

    public FilteredJobNotificationWrapper(UUID jobId, ProcessingType processingType, String channelName, List<NotificationContentWrapper> jobNotifications) {
        this.jobId = jobId;
        this.processingType = processingType;
        this.channelName = channelName;
        this.jobNotifications = jobNotifications;
    }

    public UUID getJobId() {
        return jobId;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public String getChannelName() {
        return channelName;
    }

    public List<NotificationContentWrapper> getJobNotifications() {
        return jobNotifications;
    }

}

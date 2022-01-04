/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.util.Stringable;

// NotificationContentWrapper is not serializable, so this class cannot be serializable (and doesn't need to be)
public class FilteredJobNotificationWrapper extends Stringable {
    private final UUID jobId;
    private final ProcessingType processingType;
    private final String channelName;
    private final String jobName;
    private final List<NotificationContentWrapper> jobNotifications;

    public FilteredJobNotificationWrapper(UUID jobId, ProcessingType processingType, String channelName, String jobName, List<NotificationContentWrapper> jobNotifications) {
        this.jobId = jobId;
        this.processingType = processingType;
        this.channelName = channelName;
        this.jobName = jobName;
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

    public String getJobName() {
        return jobName;
    }

    public List<NotificationContentWrapper> getJobNotifications() {
        return jobNotifications;
    }

}

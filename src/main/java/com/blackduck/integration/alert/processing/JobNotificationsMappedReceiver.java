/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.event.JobNotificationMappedEvent;
import com.blackduck.integration.alert.api.processor.event.NotificationProcessingReceiver;
import com.google.gson.Gson;

@Component(value = JobNotificationsMappedReceiver.COMPONENT_NAME)
public class JobNotificationsMappedReceiver extends NotificationProcessingReceiver<JobNotificationMappedEvent> {
    public static final String COMPONENT_NAME = "job_notification_mapping_receiver";

    public JobNotificationsMappedReceiver(
        Gson gson,
        TaskExecutor taskExecutor,
        JobNotificationMappedEventHandler eventHandler
    ) {
        super(gson, taskExecutor, JobNotificationMappedEvent.NOTIFICATION_MAPPED_EVENT_TYPE, JobNotificationMappedEvent.class, eventHandler);
    }
}

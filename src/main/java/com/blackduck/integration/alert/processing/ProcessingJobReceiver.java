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

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.processor.event.JobProcessingEvent;
import com.blackduck.integration.alert.api.processor.event.NotificationProcessingReceiver;

@Component
public class ProcessingJobReceiver extends NotificationProcessingReceiver<JobProcessingEvent> {
    public ProcessingJobReceiver(
        Gson gson,
        TaskExecutor taskExecutor,
        ProcessingJobEventHandler eventHandler
    ) {
        super(gson, taskExecutor, JobProcessingEvent.JOB_PROCESSING_EVENT_TYPE, JobProcessingEvent.class, eventHandler);
    }
}

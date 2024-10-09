package com.blackduck.integration.alert.processing;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.event.JobProcessingEvent;
import com.blackduck.integration.alert.api.processor.event.NotificationProcessingReceiver;
import com.google.gson.Gson;

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

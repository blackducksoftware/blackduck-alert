package com.synopsys.integration.alert.api.distribution.execution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

@Component
public class JobStageStartedEventListener extends AlertMessageListener<JobStageStartedEvent> {
    @Autowired
    public JobStageStartedEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        JobStageStartedHandler eventHandler
    ) {
        super(gson, taskExecutor, JobStageStartedEvent.DEFAULT_DESTINATION_NAME, JobStageStartedEvent.class, eventHandler);
    }
}

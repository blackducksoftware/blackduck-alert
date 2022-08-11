package com.synopsys.inegration.alert.api.distribution;

import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.api.event.AlertMessageListener;
import com.synopsys.integration.alert.api.event.distribution.JobSubTaskEvent;

public abstract class JobSubTaskMessageListener<T extends JobSubTaskEvent> extends AlertMessageListener<T> {

    protected JobSubTaskMessageListener(
        Gson gson,
        TaskExecutor taskExecutor,
        String destinationName,
        Class<T> eventClass,
        AlertEventHandler<T> eventHandler
    ) {
        super(gson, taskExecutor, destinationName, eventClass, eventHandler);
    }

}

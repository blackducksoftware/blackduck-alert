package com.blackduck.integration.alert.api.processor.event;

import org.springframework.core.task.TaskExecutor;

import com.blackduck.integration.alert.api.event.AlertEvent;
import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.google.gson.Gson;

public abstract class NotificationProcessingReceiver<T extends AlertEvent> extends AlertMessageListener<T> {
    protected NotificationProcessingReceiver(
        Gson gson,
        TaskExecutor taskExecutor,
        String destinationName,
        Class<T> eventClass,
        AlertEventHandler<T> eventHandler
    ) {
        super(gson, taskExecutor, destinationName, eventClass, eventHandler);
    }
}

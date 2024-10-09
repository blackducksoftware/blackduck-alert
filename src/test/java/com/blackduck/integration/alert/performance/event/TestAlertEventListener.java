package com.blackduck.integration.alert.performance.event;

import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.event.AlertMessageListener;

public class TestAlertEventListener extends AlertMessageListener<TestAlertEvent> {
    private final TestAlertEventHandler handler;

    public TestAlertEventListener(
        Gson gson, TaskExecutor taskExecutor, String destinationName,
        TestAlertEventHandler eventHandler
    ) {
        super(gson, taskExecutor, destinationName, TestAlertEvent.class, eventHandler);
        this.handler = eventHandler;
    }

    public TestAlertEventHandler getHandler() {
        return handler;
    }
}

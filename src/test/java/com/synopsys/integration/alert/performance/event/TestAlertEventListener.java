package com.synopsys.integration.alert.performance.event;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

public class TestAlertEventListener extends AlertMessageListener<TestAlertEvent> {
    private final TestAlertEventHandler handler;

    public TestAlertEventListener(Gson gson, String destinationName,
        TestAlertEventHandler eventHandler) {
        super(gson, destinationName, TestAlertEvent.class, eventHandler);
        this.handler = eventHandler;
    }

    public TestAlertEventHandler getHandler() {
        return handler;
    }
}

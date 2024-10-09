package com.blackduck.integration.alert.performance.event;

import com.blackduck.integration.alert.api.event.AlertEvent;

public class TestAlertEvent extends AlertEvent {
    private String content;

    public TestAlertEvent(String destinationName, String content) {
        super(destinationName);
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

package com.synopsys.integration.alert.performance.event;

import com.synopsys.integration.alert.api.event.AlertEvent;

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

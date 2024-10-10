/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
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

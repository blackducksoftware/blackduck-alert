/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.google.gson.Gson;

@Component
public class AuditSuccessEventListener extends AlertMessageListener<AuditSuccessEvent> {

    @Autowired
    public AuditSuccessEventListener(
        Gson gson,
        TaskExecutor taskExecutor,
        AuditSuccessHandler eventHandler
    ) {
        super(gson, taskExecutor, AuditSuccessEvent.DEFAULT_DESTINATION_NAME, AuditSuccessEvent.class, eventHandler);
    }
}

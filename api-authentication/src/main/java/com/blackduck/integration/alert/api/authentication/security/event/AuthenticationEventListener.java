/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.authentication.security.event;

import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.google.gson.Gson;

@Component
public class AuthenticationEventListener extends AlertMessageListener<AlertAuthenticationEvent> {
    public static final String DESTINATION_NAME = "AuthenticationEventListener";

    public AuthenticationEventListener(Gson gson, TaskExecutor taskExecutor, AuthenticationEventHandler eventHandler) {
        super(gson, taskExecutor, DESTINATION_NAME, AlertAuthenticationEvent.class, eventHandler);
    }

}

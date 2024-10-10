/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution;

import org.springframework.core.task.TaskExecutor;

import com.blackduck.integration.alert.api.event.AlertEventHandler;
import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.api.event.distribution.JobSubTaskEvent;
import com.google.gson.Gson;

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

/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.processor.event;

import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.AlertEvent;
import com.synopsys.integration.alert.api.event.AlertEventHandler;
import com.synopsys.integration.alert.api.event.AlertMessageListener;

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

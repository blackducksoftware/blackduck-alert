/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;

@Component(value = NotificationReceiver.COMPONENT_NAME)
public class NotificationReceiver extends AlertMessageListener<NotificationReceivedEvent> {
    public static final String COMPONENT_NAME = "notification_receiver";

    @Autowired
    public NotificationReceiver(Gson gson, TaskExecutor taskExecutor, NotificationReceivedEventHandler eventHandler) {
        super(gson, taskExecutor, NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE, NotificationReceivedEvent.class, eventHandler);
    }

}

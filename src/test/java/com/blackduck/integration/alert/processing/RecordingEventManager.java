/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import com.blackduck.integration.alert.api.event.AlertEvent;
import com.blackduck.integration.alert.api.event.EventManager;
import com.google.gson.Gson;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.TaskExecutor;

import java.util.ArrayList;

public class RecordingEventManager extends EventManager {
    private final ArrayList<AlertEvent> eventList;

    public RecordingEventManager(Gson gson, RabbitTemplate rabbitTemplate, TaskExecutor taskExecutor) {
        super(gson, rabbitTemplate, taskExecutor);
        eventList = new ArrayList<>();
    }

    @Override
    public void sendEvent(AlertEvent event) {
        eventList.add(event);
        super.sendEvent(event);
    }

    public ArrayList<AlertEvent> getEventList() {
        return eventList;
    }

    public void clearEventList() {
        eventList.clear();
    }
}

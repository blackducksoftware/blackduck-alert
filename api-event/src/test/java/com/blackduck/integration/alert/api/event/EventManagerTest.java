/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.event;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;

import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class EventManagerTest {
    @Test
    void testSendEvents() {
        String testDestination = "destination";
        AlertEvent testEvent = new AlertEvent(testDestination);

        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));

        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        String testEventJson = gson.toJson(testEvent);

        EventManager eventManager = new EventManager(gson, rabbitTemplate, new SyncTaskExecutor());
        eventManager.sendEvents(List.of(testEvent));

        Mockito.verify(rabbitTemplate, Mockito.times(1)).convertAndSend(testDestination, testEventJson);
    }

}

/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;

import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class AlertMessageListenerTest {
    private static final String DESTINATION = "test_destination_event";

    private AlertEventHandler<AlertEvent> eventHandler;
    private AlertMessageListener<AlertEvent> mockListener;
    private AlertEvent event;
    private Message message;

    @BeforeEach
    void setUp() {
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        eventHandler = Mockito.mock(AlertEventHandler.class);
        mockListener = new AlertMessageListener<>(gson, new SyncTaskExecutor(), DESTINATION, AlertEvent.class, eventHandler) {};
        event = new AlertEvent(DESTINATION);
        message = new Message(gson.toJson(event).getBytes());
    }

    @Test
    void calculateAverageMessageSizeReturnsZeroWhenNoMessagesReceived() {
        assertEquals(0.0, mockListener.calculateAverageMessageSize(), "Average message size should be 0.0 before any messages are received");
    }

    @Test
    void getHandedMessageCountReturnsZeroBeforeAnyMessagesReceived() {
        assertEquals(0L, mockListener.getHandedMessageCount(), "Message count should be 0 before any messages are received");
    }

    @Test
    void getHandedMessageCountIncrementsForEachMessageReceived() {
        mockListener.onMessage(message);
        mockListener.onMessage(message);
        mockListener.onMessage(message);

        assertEquals(3L, mockListener.getHandedMessageCount(), "Message count should equal the number of messages received");
    }

    @Test
    void calculateAverageMessageSizeReturnsCorrectAverageAfterSingleMessage() {
        mockListener.onMessage(message);

        assertEquals(message.getBody().length, mockListener.calculateAverageMessageSize(), "Average should equal the single message size when only one message has been received");
    }

    @Test
    void calculateAverageMessageSizeReturnsCorrectAverageAfterMultipleMessages() {
        mockListener.onMessage(message);
        mockListener.onMessage(message);

        assertEquals(message.getBody().length, mockListener.calculateAverageMessageSize(), "Average message size should be the mean of all received message sizes");
    }

    @Test
    void onMessageInvokesEventHandlerWithDeserializedEvent() {
        mockListener.onMessage(message);

        Mockito.verify(eventHandler, Mockito.times(1))
            .handle(Mockito.argThat(received -> received.getEventId().equals(event.getEventId()) && received.getDestination().equals(event.getDestination())));
    }

    @Test
    void onMessageDoesNotPropagateExceptionWhenHandlerThrows() {
        Mockito.doThrow(new RuntimeException("handler failure")).when(eventHandler).handle(Mockito.any());

        assertDoesNotThrow(() -> mockListener.onMessage(message), "Exceptions from the event handler should be caught and not propagated to the caller");
    }

}

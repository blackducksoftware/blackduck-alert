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

import com.google.gson.Gson;

class AlertMessageListenerTest {
    private static final String DESTINATION = "test_destination_event";

    private AlertEventHandler<AlertEvent> mockEventHandler;
    private AlertMessageListener<AlertEvent> alertMessageListener;
    private AlertEvent event;
    private Message message;

    @BeforeEach
    void setUp() {
        Gson gson = new Gson();
        mockEventHandler = Mockito.mock(AlertEventHandler.class);
        alertMessageListener = new AlertMessageListener<>(gson, new SyncTaskExecutor(), DESTINATION, AlertEvent.class, mockEventHandler) {};
        event = new AlertEvent(DESTINATION);
        message = new Message(gson.toJson(event).getBytes());
    }

    @Test
    void calculateAverageMessageSizeReturnsZeroWhenNoMessagesReceived() {
        assertEquals(0.0, alertMessageListener.calculateAverageMessageSize(), "Average message size should be 0.0 before any messages are received");
    }

    @Test
    void getHandedMessageCountReturnsZeroBeforeAnyMessagesReceived() {
        assertEquals(0L, alertMessageListener.getHandedMessageCount(), "Message count should be 0 before any messages are received");
    }

    @Test
    void getHandedMessageCountIncrementsForEachMessageReceived() {
        alertMessageListener.onMessage(message);
        alertMessageListener.onMessage(message);
        alertMessageListener.onMessage(message);

        assertEquals(3L, alertMessageListener.getHandedMessageCount(), "Message count should equal the number of messages received");
    }

    @Test
    void calculateAverageMessageSizeReturnsCorrectAverageAfterSingleMessage() {
        alertMessageListener.onMessage(message);

        assertEquals(
            message.getBody().length,
            alertMessageListener.calculateAverageMessageSize(),
            "Average should equal the single message size when only one message has been received"
        );
    }

    @Test
    void calculateAverageMessageSizeReturnsCorrectAverageAfterMultipleMessages() {
        alertMessageListener.onMessage(message);
        alertMessageListener.onMessage(message);

        assertEquals(message.getBody().length, alertMessageListener.calculateAverageMessageSize(), "Average message size should be the mean of all received message sizes");
    }

    @Test
    void onMessageInvokesEventHandlerWithDeserializedEvent() {
        alertMessageListener.onMessage(message);

        Mockito.verify(mockEventHandler, Mockito.times(1))
            .handle(Mockito.argThat(received -> received.getEventId().equals(event.getEventId()) && received.getDestination().equals(event.getDestination())));
    }

    @Test
    void onMessageDoesNotPropagateExceptionWhenHandlerThrows() {
        Mockito.doThrow(new RuntimeException("handler failure")).when(mockEventHandler).handle(Mockito.any());

        assertDoesNotThrow(() -> alertMessageListener.onMessage(message), "Exceptions from the event handler should be caught and not propagated to the caller");
    }

}

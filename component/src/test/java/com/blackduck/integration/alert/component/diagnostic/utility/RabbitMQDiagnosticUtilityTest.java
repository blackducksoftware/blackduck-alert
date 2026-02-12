/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;

import com.blackduck.integration.alert.api.event.AlertMessageListener;
import com.blackduck.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.blackduck.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;

class RabbitMQDiagnosticUtilityTest {
    private final String destinationName = "queue1";

    @Test
    void getRabbitMQDiagnosticsTest() {
        AmqpAdmin amqpAdmin = Mockito.mock(AmqpAdmin.class);
        AlertMessageListener<?> alertMessageListener = Mockito.mock(AlertMessageListener.class);

        int messageCount = 5;
        int consumerCount = 1;
        double averageMessageSize = 25.0;
        QueueInformation queueInformation = new QueueInformation(destinationName, messageCount, consumerCount);

        Mockito.when(alertMessageListener.getDestinationName()).thenReturn(destinationName);
        Mockito.when(alertMessageListener.calculateAverageMessageSize()).thenReturn(averageMessageSize);
        Mockito.when(amqpAdmin.getQueueInfo(destinationName)).thenReturn(queueInformation);

        RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility = new RabbitMQDiagnosticUtility(amqpAdmin, List.of(alertMessageListener));

        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        assertEquals(1, rabbitMQDiagnosticModel.getQueues().size());
        AlertQueueInformation alertQueueInformation = rabbitMQDiagnosticModel.getQueues().get(0);
        assertEquals(destinationName, alertQueueInformation.getName());
        assertEquals(messageCount, alertQueueInformation.getMessageCount());
        assertEquals(consumerCount, alertQueueInformation.getConsumerCount());
        assertEquals(averageMessageSize, alertQueueInformation.getAverageMessageSizeBytes());
    }

    @Test
    void getRabbitMQDiagnosticsNoListeners() {
        AmqpAdmin amqpAdmin = Mockito.mock(AmqpAdmin.class);

        RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility = new RabbitMQDiagnosticUtility(amqpAdmin, List.of());
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        assertTrue(rabbitMQDiagnosticModel.getQueues().isEmpty());
    }

    @Test
    void getRabbitMQDiagnosticsNoQueueInfo() {
        AmqpAdmin amqpAdmin = Mockito.mock(AmqpAdmin.class);
        AlertMessageListener<?> alertMessageListener = Mockito.mock(AlertMessageListener.class);

        Mockito.when(alertMessageListener.getDestinationName()).thenReturn(destinationName);
        Mockito.when(amqpAdmin.getQueueInfo(Mockito.anyString())).thenReturn(null);

        RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility = new RabbitMQDiagnosticUtility(amqpAdmin, List.of());
        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        assertTrue(rabbitMQDiagnosticModel.getQueues().isEmpty());
    }
}

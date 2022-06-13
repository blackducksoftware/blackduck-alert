package com.synopsys.integration.alert.component.diagnostic.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;

import com.synopsys.integration.alert.api.event.AlertMessageListener;
import com.synopsys.integration.alert.component.diagnostic.model.AlertQueueInformation;
import com.synopsys.integration.alert.component.diagnostic.model.RabbitMQDiagnosticModel;

class RabbitMQDiagnosticUtilityTest {
    private final String destinationName = "queue1";

    @Test
    void getRabbitMQDiagnosticsTest() {
        AmqpAdmin amqpAdmin = Mockito.mock(AmqpAdmin.class);
        AlertMessageListener<?> alertMessageListener = Mockito.mock(AlertMessageListener.class);

        int messageCount = 5;
        int consumerCount = 1;
        QueueInformation queueInformation = new QueueInformation(destinationName, messageCount, consumerCount);

        Mockito.when(alertMessageListener.getDestinationName()).thenReturn(destinationName);
        Mockito.when(amqpAdmin.getQueueInfo(destinationName)).thenReturn(queueInformation);

        RabbitMQDiagnosticUtility rabbitMQDiagnosticUtility = new RabbitMQDiagnosticUtility(amqpAdmin, List.of(alertMessageListener));

        RabbitMQDiagnosticModel rabbitMQDiagnosticModel = rabbitMQDiagnosticUtility.getRabbitMQDiagnostics();
        assertEquals(1, rabbitMQDiagnosticModel.getQueues().size());
        AlertQueueInformation alertQueueInformation = rabbitMQDiagnosticModel.getQueues().get(0);
        assertEquals(destinationName, alertQueueInformation.getName());
        assertEquals(messageCount, alertQueueInformation.getMessageCount());
        assertEquals(consumerCount, alertQueueInformation.getConsumerCount());
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

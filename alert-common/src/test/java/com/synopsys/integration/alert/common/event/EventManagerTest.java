package com.synopsys.integration.alert.common.event;

import java.util.List;
import java.util.function.Consumer;

import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;

public class EventManagerTest {
    @Test
    public void testSendEvents() {
        String testDestination = "destination";
        AlertEvent testEvent = new AlertEvent(testDestination);

        JmsTemplate jmsTemplate = Mockito.mock(JmsTemplate.class);
        Mockito.doNothing().when(jmsTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));

        Gson gson = new Gson();
        String testEventJson = gson.toJson(testEvent);

        ContentConverter contentConverter = Mockito.mock(ContentConverter.class);
        Mockito.when(contentConverter.getJsonString(Mockito.any(AlertEvent.class))).thenReturn(testEventJson);
        BrokerServiceTaskFactory taskFactory = new BrokerServiceTaskFactory() {
            @Override
            public BrokerServiceDependentTask createTask(String taskName, BrokerServiceWaitTask brokerServiceWaitTask, Consumer<BrokerService> task) {
                return super.createTask(taskName, new BrokerServiceWaitTask() {
                    @Override
                    public boolean isComplete() {
                        return true;
                    }
                }, (brokerService) -> {
                    // call the original task from the factory
                    task.accept(brokerService);
                    // verify the event was sent
                    Mockito.verify(jmsTemplate, Mockito.times(1)).convertAndSend(Mockito.eq(testDestination), Mockito.eq(testEventJson));
                });
            }
        };

        EventManager eventManager = new EventManager(contentConverter, jmsTemplate, taskFactory);

        eventManager.sendEvents(List.of(testEvent));
    }

}

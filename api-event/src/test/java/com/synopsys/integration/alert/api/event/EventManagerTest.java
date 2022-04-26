package com.synopsys.integration.alert.api.event;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.google.gson.Gson;

public class EventManagerTest {
    @Test
    public void testSendEvents() {
        String testDestination = "destination";
        AlertEvent testEvent = new AlertEvent(testDestination);

        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));

        Gson gson = new Gson();
        String testEventJson = gson.toJson(testEvent);

        EventManager eventManager = new EventManager(gson, rabbitTemplate);
        eventManager.sendEvents(List.of(testEvent));

        Mockito.verify(rabbitTemplate, Mockito.times(1)).convertAndSend(Mockito.eq(testDestination), Mockito.eq(testEventJson));
    }

}

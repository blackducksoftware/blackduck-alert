package com.synopsys.integration.alert.api.event;

import java.util.List;

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

        EventManager eventManager = new EventManager(contentConverter, jmsTemplate);

        eventManager.sendEvents(List.of(testEvent));

        Mockito.verify(jmsTemplate, Mockito.times(1)).convertAndSend(Mockito.eq(testDestination), Mockito.eq(testEventJson));
    }

}

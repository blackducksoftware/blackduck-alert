package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertEquals;

import javax.jms.ConnectionFactory;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.event.RealTimeEvent;

public class RealTimeJMSTemplateTest {

    @Test
    public void testGetDestinationName() {
        final ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        final RealTimeJmsTemplate realTimeJmsTemplate = new RealTimeJmsTemplate(connectionFactory);

        final String actualDestinationName = realTimeJmsTemplate.getDestinationName();
        final String expectedDestinationName = RealTimeEvent.TOPIC_NAME;

        assertEquals(expectedDestinationName, actualDestinationName);
    }
}

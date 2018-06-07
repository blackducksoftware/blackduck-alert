package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertEquals;

import javax.jms.ConnectionFactory;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.event.InternalEventTypes;

public class RealTimeJMSTemplateTest {

    @Test
    public void testGetDestinationName() {
        final ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        final RealTimeJmsTemplate realTimeJmsTemplate = new RealTimeJmsTemplate(connectionFactory);

        final String actualDestinationName = realTimeJmsTemplate.getDestinationName();
        final String expectedDestinationName = InternalEventTypes.REAL_TIME_EVENT.getDestination();

        assertEquals(expectedDestinationName, actualDestinationName);
    }
}

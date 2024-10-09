package com.blackduck.integration.alert.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.event.AlertEvent;

public class AlertEventTest {
    private static final String TOPIC = "TOPIC";

    @Test
    public void getIdTest() {
        AlertEvent event = new AlertEvent(TOPIC);
        assertNotNull(event.getEventId());
    }

    @Test
    public void getDestinationTest() {
        AlertEvent event = new AlertEvent(TOPIC);
        assertEquals(TOPIC, event.getDestination());

    }

}

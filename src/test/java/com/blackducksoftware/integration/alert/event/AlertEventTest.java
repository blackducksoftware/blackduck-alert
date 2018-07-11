/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.alert.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blackducksoftware.integration.alert.event.AlertEvent;

public class AlertEventTest {
    private static final String TOPIC = "TOPIC";

    @Test
    public void getIdTest() {
        final AlertEvent event = new AlertEvent(TOPIC, null);
        assertNotNull(event.getEventId());
    }

    @Test
    public void getDestinationTest() {
        final AlertEvent event = new AlertEvent(TOPIC, null);
        assertEquals(TOPIC, event.getDestination());
    }

}

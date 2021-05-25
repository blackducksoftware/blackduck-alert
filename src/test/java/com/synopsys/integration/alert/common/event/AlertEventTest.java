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
package com.synopsys.integration.alert.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.event.AlertEvent;

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

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
package com.blackducksoftware.integration.hub.alert.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AbstractEventTest {
    private static final String TOPIC = "TOPIC";

    @Test
    public void getIdTest() {
        final AbstractEvent event = new AbstractEvent() {
            @Override
            public String getTopic() {
                return null;
            }
        };
        assertNotNull(event.getEventId());
    }

    @Test
    public void getTopicTest() {
        final AbstractEvent event = new AbstractEvent() {
            @Override
            public String getTopic() {
                return TOPIC;
            }
        };
        assertEquals(TOPIC, event.getTopic());
    }

}

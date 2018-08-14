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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AlertEventTest {
    private static final String TOPIC = "TOPIC";
    private static final String PROVIDER = "PROVIDER";
    private static final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private static final String CONTENT = "CONTENT";
    private static final String CREATED_AT = "CREATED_AT_STRING";

    @Test
    public void getIdTest() {
        final AlertEvent event = new AlertEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertNotNull(event.getEventId());
    }

    @Test
    public void getDestinationTest() {
        final AlertEvent event = new AlertEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertEquals(TOPIC, event.getDestination());
    }

    @Test
    public void getCreatedAtDateTest() {
        final AlertEvent event = new AlertEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertEquals(CREATED_AT, event.getCreatedAt());
    }

    @Test
    public void getProviderTest() {
        final AlertEvent event = new AlertEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertEquals(PROVIDER, event.getProvider());
    }

    @Test
    public void getNotificationTypeTest() {
        final AlertEvent event = new AlertEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, 1L);
        assertEquals(NOTIFICATION_TYPE, event.getNotificationType());
    }

    @Test
    public void getNotificationIdTest() {
        final Long notificationId = new Long(1);
        final AlertEvent event = new AlertEvent(TOPIC, CREATED_AT, PROVIDER, NOTIFICATION_TYPE, CONTENT, notificationId);
        assertEquals(notificationId, event.getNotificationId());
    }
}

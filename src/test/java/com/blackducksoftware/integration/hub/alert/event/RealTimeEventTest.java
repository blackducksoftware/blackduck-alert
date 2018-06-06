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

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;

public class RealTimeEventTest {

    @Test
    public void getNotificationListTest() {
        final List<NotificationModel> list = Collections.emptyList();
        final RealTimeEvent event = new RealTimeEvent(list);
        assertEquals(list, event.getNotificationList());
    }

    @Test
    public void getTopicTest() {
        final RealTimeEvent event = new RealTimeEvent(null);
        assertEquals(RealTimeEvent.TOPIC_NAME, event.getDestination());
    }

}

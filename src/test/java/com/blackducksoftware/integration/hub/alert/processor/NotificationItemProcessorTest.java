/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.processor;

import java.util.Collections;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.test.TestLogger;

public class NotificationItemProcessorTest {

    @Test
    public void testInit() {
        final GlobalProperties globalProperties = new TestGlobalProperties();
        final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(globalProperties, new TestLogger(), Collections.emptyList());
    }

    @Test
    public void testProcessEvents() throws HubIntegrationException {
        final GlobalProperties globalProperties = new TestGlobalProperties();

        // TODO fix Test
        // final NotificationResults results = new NotificationResults(Collections.emptyList(), null);
        // final NotificationEvent event1 = new NotificationEvent("event 1", NotificationCategoryEnum.HIGH_VULNERABILITY, null);
        // final NotificationEvent event2 = new NotificationEvent("event 2", NotificationCategoryEnum.LOW_VULNERABILITY, null);
        // final List<NotificationEvent> eventList = Arrays.asList(event1, event2);

        // final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(globalProperties, new TestLogger(), Collections.emptyList());
        // final DBStoreEvent storeEvent = notificationItemProcessor.process(results);

        // assertEquals("DB_STORE_EVENT", storeEvent.getTopic());
        // assertTrue(storeEvent.getNotificationList().size() == 2);
    }
}

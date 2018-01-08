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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.api.project.ProjectAssignmentService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.test.TestLogger;

public class NotificationItemProcessorTest {

    @Test
    public void testInit() {
        final ProjectService projectService = Mockito.mock(ProjectService.class);
        final ProjectAssignmentService projectAssignmentService = Mockito.mock(ProjectAssignmentService.class);
        final HubService hubService = Mockito.mock(HubService.class);
        final VulnerabilityService vulnerabilityService = Mockito.mock(VulnerabilityService.class);
        final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(projectService, projectAssignmentService, hubService, vulnerabilityService, new TestLogger());

        assertTrue(notificationItemProcessor.getCacheList().size() == 2);
        assertTrue(notificationItemProcessor.getProcessorMap().size() == 4);
    }

    @Test
    public void testProcessEvents() throws HubIntegrationException {
        final NotificationEvent event1 = new NotificationEvent("event 1", NotificationCategoryEnum.HIGH_VULNERABILITY, null);
        final NotificationEvent event2 = new NotificationEvent("event 2", NotificationCategoryEnum.LOW_VULNERABILITY, null);
        final List<NotificationEvent> eventList = Arrays.asList(event1, event2);

        final NotificationItemProcessor notificationItemProcessor = new NotificationItemProcessor(null, null, null, null, new TestLogger());
        final DBStoreEvent storeEvent = notificationItemProcessor.processEvents(eventList);

        assertEquals("DB_STORE_EVENT", storeEvent.getTopic());
        assertTrue(storeEvent.getNotificationList().size() == 2);
    }
}

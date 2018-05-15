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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.throwaway.UserNotificationCache;
import com.blackducksoftware.integration.hub.api.generated.view.AssignedUserView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.throwaway.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.throwaway.NotificationContentItem;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.hub.throwaway.ProjectVersionModel;

public class UserNotificationCacheTest {

    @Test
    public void testAddUserInformation() throws IntegrationException {
        final ProjectService mockedProjectService = Mockito.mock(ProjectService.class);
        final UserNotificationCache userNotificationCache = new UserNotificationCache(mockedProjectService);

        final AssignedUserView assignedUser = new AssignedUserView();
        assignedUser.name = "test name";
        final List<AssignedUserView> assignedUsersList = Arrays.asList(assignedUser);
        Mockito.when(mockedProjectService.getAssignedUsersToProject(Mockito.anyString())).thenReturn(assignedUsersList);

        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        projectVersionModel.setProjectLink("New project link");
        final String componentName = "notification test";
        final ComponentVersionView componentVersionView = new ComponentVersionView();
        final String componentVersionUrl = "sss";
        final String componentIssueUrl = "ddd";

        final Map<String, Object> dataSet = new HashMap<>();
        dataSet.put(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT, new NotificationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentVersionUrl, componentIssueUrl));
        final NotificationEvent notificationEvent = new NotificationEvent("key", NotificationCategoryEnum.HIGH_VULNERABILITY, dataSet);

        final List<NotificationEvent> notificationEvents = Arrays.asList(notificationEvent);
        Collection<NotificationEvent> notEmptyEventList = Arrays.asList();

        assertTrue(notEmptyEventList.size() == 0);

        notEmptyEventList = userNotificationCache.addUserInformation(notificationEvents);

        assertTrue(notEmptyEventList.size() == 1);
    }

    @Test
    public void testAddUserInformationException() throws Exception {
        try (OutputLogger outputLogger = new OutputLogger()) {
            final ProjectService mockedProjectService = Mockito.mock(ProjectService.class);
            final UserNotificationCache userNotificationCache = new UserNotificationCache(mockedProjectService);

            Mockito.doThrow(new IntegrationException()).when(mockedProjectService).getAssignedUsersToProject(Mockito.anyString());

            final Date createdAt = new Date();
            final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
            projectVersionModel.setProjectLink("New project link");
            final String componentName = "notification test";
            final ComponentVersionView componentVersionView = new ComponentVersionView();
            final String componentVersionUrl = "sss";
            final String componentIssueUrl = "ddd";

            final Map<String, Object> dataSet = new HashMap<>();
            dataSet.put(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT, new NotificationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentVersionUrl, componentIssueUrl));
            final NotificationEvent notificationEvent = new NotificationEvent("key", NotificationCategoryEnum.HIGH_VULNERABILITY, dataSet);

            final List<NotificationEvent> notificationEvents = Arrays.asList(notificationEvent);
            Collection<NotificationEvent> emptyEventList = Arrays.asList();

            assertEquals(0, emptyEventList.size());

            emptyEventList = userNotificationCache.addUserInformation(notificationEvents);

            assertEquals(0, emptyEventList.size());
            assertTrue(outputLogger.isLineContainingText("Error getting the users for project"));
        }
    }

}

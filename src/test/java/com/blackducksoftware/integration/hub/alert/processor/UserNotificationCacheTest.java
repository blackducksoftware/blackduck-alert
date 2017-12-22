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
import com.blackducksoftware.integration.hub.api.project.ProjectAssignmentService;
import com.blackducksoftware.integration.hub.api.project.ProjectService;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.model.view.AssignedUserView;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;

public class UserNotificationCacheTest {

    @Test
    public void testAddUserInformation() throws IntegrationException {
        final ProjectService mockedProjectService = Mockito.mock(ProjectService.class);
        final ProjectAssignmentService mockedProjectAssignmentService = Mockito.mock(ProjectAssignmentService.class);
        final UserNotificationCache userNotificationCache = new UserNotificationCache(mockedProjectService, mockedProjectAssignmentService);

        final ProjectView projectView = new ProjectView();
        Mockito.when(mockedProjectService.getView(Mockito.anyString(), Mockito.any())).thenReturn(projectView);

        final AssignedUserView assignedUser = new AssignedUserView();
        assignedUser.name = "test name";
        final List<AssignedUserView> assignedUsersList = Arrays.asList(assignedUser);
        Mockito.when(mockedProjectAssignmentService.getProjectUsers(projectView)).thenReturn(assignedUsersList);

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

}

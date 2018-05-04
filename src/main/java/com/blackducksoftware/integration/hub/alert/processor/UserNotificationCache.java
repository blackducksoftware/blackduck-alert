/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.alert.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.AlertConstants;
import com.blackducksoftware.integration.hub.api.generated.view.AssignedUserView;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.ProjectService;
import com.blackducksoftware.integration.hub.throwaway.MapProcessorCache;
import com.blackducksoftware.integration.hub.throwaway.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.throwaway.NotificationContentItem;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;

public class UserNotificationCache extends MapProcessorCache {
    private final Logger logger = LoggerFactory.getLogger(UserNotificationCache.class);
    private final ProjectService projectService;

    public UserNotificationCache(final ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public Collection<NotificationEvent> getEvents() throws HubIntegrationException {
        return addUserInformation(super.getEvents());
    }

    public Collection<NotificationEvent> addUserInformation(final Collection<NotificationEvent> notificationEvents) {
        final List<NotificationEvent> userEventList = new ArrayList<>();

        final String key = AlertConstants.DATASET_KEY_HUB_USER;
        notificationEvents.forEach(currentNotification -> {
            final NotificationContentItem notificationContentItem = (NotificationContentItem) currentNotification.getDataSet().get(NotificationEvent.DATA_SET_KEY_NOTIFICATION_CONTENT);
            final String projectName = notificationContentItem.getProjectVersion().getProjectName();
            final List<String> userNameList = getUserNames(projectName);
            userNameList.forEach(userName -> {
                final String eventKey = currentNotification.getEventKey();
                final NotificationCategoryEnum categoryType = currentNotification.getCategoryType();
                final Map<String, Object> dataSet = new HashMap<>(currentNotification.getDataSet());
                dataSet.put(key, userName);
                final NotificationEvent userEvent = new NotificationEvent(eventKey, categoryType, dataSet);
                userEventList.add(userEvent);
            });
        });
        return userEventList;
    }

    private List<String> getUserNames(final String projectName) {
        final List<String> userNameList = new ArrayList<>();
        try {
            final List<AssignedUserView> assignedUserList = projectService.getAssignedUsersToProject(projectName);
            if (!assignedUserList.isEmpty()) {
                assignedUserList.forEach(assignedUser -> {
                    userNameList.add(assignedUser.name);
                });
            }
        } catch (final IntegrationException ex) {
            logger.debug("Error getting the users for project {}", projectName);
            logger.debug("Caused by:", ex);
        }

        return userNameList;
    }
}

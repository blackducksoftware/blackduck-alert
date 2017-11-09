/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.email.EmailEvent;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackEvent;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.DatabaseRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserEmailRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserHipChatRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserProjectVersionsRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.HubUserSlackRelation;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.HubUserRelationRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

@Component
public class EventManager {
    private final HubUsersRepository hubUsersRepository;
    private final HubUserRelationRepository<HubUserProjectVersionsRelation> projectVersionRelationRepository;
    private final HubUserRelationRepository<HubUserEmailRelation> emailRelationRepository;
    private final HubUserRelationRepository<HubUserHipChatRelation> hipChatRelationRepository;
    private final HubUserRelationRepository<HubUserSlackRelation> slackRelationRepository;

    @Autowired
    public EventManager(final HubUsersRepository hubUsersRepository, final HubUserRelationRepository<HubUserProjectVersionsRelation> projectVersionRelationRepository,
            final HubUserRelationRepository<HubUserEmailRelation> emailRelationRepository, final HubUserRelationRepository<HubUserHipChatRelation> hipChatRelationRepository,
            final HubUserRelationRepository<HubUserSlackRelation> slackRelationRepository) {
        this.hubUsersRepository = hubUsersRepository;
        this.projectVersionRelationRepository = projectVersionRelationRepository;
        this.emailRelationRepository = emailRelationRepository;
        this.hipChatRelationRepository = hipChatRelationRepository;
        this.slackRelationRepository = slackRelationRepository;
    }

    public List<AbstractChannelEvent> createChannelEvents(final Collection<UserNotificationWrapper> userNotificationList) {
        final List<AbstractChannelEvent> channelEvents = new ArrayList<>();
        final Set<UserNotificationWrapper> filteredUserNotifications = new HashSet<>();
        userNotificationList.forEach(userNotification -> {
            if (doesUserNotificationApply(userNotification)) {
                filteredUserNotifications.add(userNotification);
            }
        });

        // Keep these ids until we support an implementation with configurations on a per-user basis
        final Long hipChatConfigId = new Long(0);
        final Long slackConfigId = new Long(0);
        final Set<ProjectData> hipChatProjectData = mergeUserNotifications(filteredUserNotifications, hipChatRelationRepository);
        final Set<ProjectData> slackProjectData = mergeUserNotifications(filteredUserNotifications, slackRelationRepository);

        channelEvents.addAll(createUserHipChatEvents(hipChatProjectData, hipChatConfigId));
        channelEvents.addAll(createUserSlackEvents(slackProjectData, slackConfigId));
        channelEvents.addAll(createUserEmailEvents(filteredUserNotifications));

        return channelEvents;
    }

    private boolean doesUserNotificationApply(final UserNotificationWrapper userNotification) {
        final HubUsersEntity hubUser = hubUsersRepository.findOne(userNotification.getUserConfigId());
        if (hubUser != null) {
            final Set<ProjectData> notificationsForUser = userNotification.getNotifications();
            notificationsForUser.forEach(notification -> {
                if (!isProjectVersionConfigured(userNotification.getUserConfigId(), notification)) {
                    notificationsForUser.remove(notification);
                }
            });
            if (!notificationsForUser.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isProjectVersionConfigured(final Long configId, final ProjectData userNotification) {
        final HubUserProjectVersionsRelation projectVersionsRelation = projectVersionRelationRepository.findOne(configId);
        if (projectVersionsRelation.getProjectName().equals(userNotification.getProjectName())) {
            if (projectVersionsRelation.getProjectVersionName().equals(userNotification.getProjectVersion())) {
                return true;
            }
        }
        return false;
    }

    private Set<EmailEvent> createUserEmailEvents(final Collection<UserNotificationWrapper> userNotificationList) {
        final Set<EmailEvent> events = new HashSet<>();
        userNotificationList.forEach(userNotification -> {
            if (emailRelationRepository.exists(userNotification.getUserConfigId())) {
                userNotification.getNotifications().forEach(notification -> {
                    events.add(new EmailEvent(notification, userNotification.getUserConfigId()));
                });
            }
        });
        return events;
    }

    private Set<HipChatEvent> createUserHipChatEvents(final Set<ProjectData> projectDataSet, final Long configId) {
        final Set<HipChatEvent> events = new HashSet<>();
        projectDataSet.forEach(projectDataItem -> {
            events.add(new HipChatEvent(projectDataItem, configId));
        });
        return events;
    }

    private Set<SlackEvent> createUserSlackEvents(final Set<ProjectData> projectDataSet, final Long configId) {
        final Set<SlackEvent> events = new HashSet<>();
        projectDataSet.forEach(projectDataItem -> {
            events.add(new SlackEvent(projectDataItem, configId));
        });
        return events;
    }

    private <R extends DatabaseRelation> Set<ProjectData> mergeUserNotifications(final Collection<UserNotificationWrapper> userNotificationList, final HubUserRelationRepository<R> repository) {
        final Set<ProjectData> mergedNotifications = new HashSet<>();
        userNotificationList.forEach(userNotification -> {
            if (repository.exists(userNotification.getUserConfigId())) {
                final Set<ProjectData> notifications = userNotification.getNotifications();
                notifications.forEach(notification -> {
                    mergedNotifications.add(notification);
                });
            }
        });
        return mergedNotifications;
    }

    // TODO get rid of this once the current implementation is tested
    // private Set<ProjectData> mergeNotifications(final Collection<UserNotificationWrapper> userNotificationList) {
    // final Set<ProjectData> mergedNotifications = new HashSet<>();
    // userNotificationList.forEach(userNotification -> {
    // final Set<ProjectData> notifications = userNotification.getNotifications();
    // notifications.forEach(notification -> {
    // mergedNotifications.add(notification);
    // });
    //
    // });
    // return mergedNotifications;
    // }

}

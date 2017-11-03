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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.blackducksoftware.integration.hub.alert.channel.email.EmailEvent;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackEvent;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class EventManager {
    private final Collection<UserNotificationWrapper> userNotificationList;

    public EventManager(final Collection<UserNotificationWrapper> userNotifications) {
        this.userNotificationList = userNotifications;
    }

    public Set<EmailEvent> getUserEmailEvents() {
        final Set<EmailEvent> events = new HashSet<>();
        userNotificationList.forEach(userNotification -> {
            userNotification.getNotifications().forEach(notification -> {
                events.add(new EmailEvent(notification, userNotification.getUserConfigId()));
            });
        });
        return events;
    }

    public Set<AbstractChannelEvent> getChatChannelEvents(final Long configId) {
        final Set<ProjectData> notifications = mergeNotifications();
        final Set<AbstractChannelEvent> channelEvents = new HashSet<>();

        channelEvents.addAll(getUserHipChatEvents(notifications, configId));
        channelEvents.addAll(getUserSlackEvents(notifications, configId));

        return channelEvents;
    }

    private Set<HipChatEvent> getUserHipChatEvents(final Set<ProjectData> projectDataSet, final Long configId) {
        final Set<HipChatEvent> events = new HashSet<>();
        projectDataSet.forEach(projectDataItem -> {
            events.add(new HipChatEvent(projectDataItem, configId));
        });
        return events;
    }

    private Set<SlackEvent> getUserSlackEvents(final Set<ProjectData> projectDataSet, final Long configId) {
        final Set<SlackEvent> events = new HashSet<>();
        projectDataSet.forEach(projectDataItem -> {
            events.add(new SlackEvent(projectDataItem, configId));
        });
        return events;
    }

    private Set<ProjectData> mergeNotifications() {
        final Set<ProjectData> mergedNotifications = new HashSet<>();
        userNotificationList.forEach(userNotification -> {
            final Set<ProjectData> notifications = userNotification.getNotifications();
            notifications.forEach(notification -> {
                mergedNotifications.add(notification);
            });

        });
        return mergedNotifications;
    }

}

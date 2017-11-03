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
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class ProjectDataFilter {
    private final Collection<ProjectData> projectDataList;

    public ProjectDataFilter(final Collection<ProjectData> projectDataList) {
        this.projectDataList = projectDataList;
    }

    public Set<AbstractChannelEvent> filterNotificationsByUser() {
        // TODO highest level transformation
        return null;
    }

    // TODO add users to project data and remove the param from this method
    public Set<AbstractChannelEvent> filterEventsByUserNotifications(final Collection<UserNotificationWrapper> userNotificationList) {
        final EventManager eventManager = new EventManager(userNotificationList);

        final Set<EmailEvent> emailEvents = eventManager.getUserEmailEvents();
        final Set<AbstractChannelEvent> chatChannelEvents = eventManager.getChatChannelEvents(new Long(0)); // TODO

        final Set<AbstractChannelEvent> events = new HashSet<>();
        events.addAll(emailEvents);
        events.addAll(chatChannelEvents);

        return events;
    }

}

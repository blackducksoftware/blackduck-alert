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
package com.blackducksoftware.integration.hub.alert.digest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.digest.filter.NotificationEventManager;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

@Component
public class DigestNotificationProcessor {
    private final ProjectDataFactory projectDataFactory;
    private final NotificationEventManager eventManager;

    @Autowired
    public DigestNotificationProcessor(final ProjectDataFactory projectDataFactory, final NotificationEventManager eventManager) {
        this.projectDataFactory = projectDataFactory;
        this.eventManager = eventManager;
    }

    public List<AbstractChannelEvent> processNotifications(final DigestTypeEnum digestType, final List<NotificationEntity> notificationList) {
        final DigestRemovalProcessor removalProcessor = new DigestRemovalProcessor();
        final List<NotificationEntity> processedNotificationList = removalProcessor.process(notificationList);
        if (processedNotificationList.isEmpty()) {
            return Collections.emptyList();
        } else {
            final Collection<ProjectData> projectDataCollection = projectDataFactory.createProjectDataCollection(processedNotificationList, digestType);
            return eventManager.createChannelEvents(projectDataCollection);
        }
    }

}

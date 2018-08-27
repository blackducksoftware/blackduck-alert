/**
 * blackduck-alert
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
package com.synopsys.integration.alert.common.digest.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.database.entity.NotificationContent;

@Component
public class NotificationPreProcessor {
    private final List<Provider> providers;

    @Autowired
    public NotificationPreProcessor(final List<Provider> providers) {
        this.providers = providers;
    }

    public List<NotificationContent> process(final Collection<NotificationContent> notificationList) {
        List<NotificationContent> filteredNotifications;
        // FIXME implement
        final Predicate<NotificationContent> notificationTypeFilter = getFilterForNotificationTypes();
        filteredNotifications = applyFilter(notificationList, notificationTypeFilter);

        // FIXME sort
        return filteredNotifications;
    }

    private Predicate<NotificationContent> getFilterForNotificationTypes() {
        // FIXME get these for real
        final List<String> notificationTypes = Collections.emptyList();
        return notificationContent -> notificationTypes.contains(notificationContent.getNotificationType());
    }

    private List<NotificationContent> applyFilter(final Collection<NotificationContent> notificationList, final Predicate<NotificationContent> filter) {
        return notificationList
                .parallelStream()
                .filter(filter)
                .collect(Collectors.toList());
    }
}

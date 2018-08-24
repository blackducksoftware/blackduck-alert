/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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

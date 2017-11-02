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
package com.blackducksoftware.integration.hub.alert.digest.filter;

import java.util.HashSet;
import java.util.Set;

import com.blackducksoftware.integration.hub.alert.channel.email.EmailEvent;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatEvent;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackEvent;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class EventManager {
    public Set<EmailEvent> getUserEmailEvents(final Set<UserNotificationWrapper> userNotifications) {
        final Set<EmailEvent> events = new HashSet<>();
        // TODO
        return events;
    }

    public Set<HipChatEvent> getUserHipChatEvents(final Set<UserNotificationWrapper> userNotifications) {
        final Set<HipChatEvent> events = new HashSet<>();
        addEvents(userNotifications, events);
        return events;
    }

    public Set<SlackEvent> getUserSlackEvents(final Set<UserNotificationWrapper> userNotifications) {
        final Set<SlackEvent> events = new HashSet<>();
        addEvents(userNotifications, events);
        return events;
    }

    private <E extends AbstractChannelEvent> void addEvents(final Set<UserNotificationWrapper> userNotifications, final Set<E> events) {
        userNotifications.forEach(userNotification -> {
            userNotification.getNotifications().forEach(notificationn -> {
                // TODO
            });
        });
    }
}

/*
 * api-event
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.event;

public class NotificationReceivedEvent extends AlertEvent {
    public static final String NOTIFICATION_RECEIVED_EVENT_TYPE = "notification_received_event";

    public NotificationReceivedEvent() {
        super(NOTIFICATION_RECEIVED_EVENT_TYPE);
    }

}

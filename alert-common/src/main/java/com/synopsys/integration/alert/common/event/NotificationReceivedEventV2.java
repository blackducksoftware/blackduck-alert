/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.event;

public class NotificationReceivedEventV2 extends AlertEvent {
    public static final String NOTIFICATION_RECEIVED_EVENT_TYPE = "notification_received_eventV2";

    public NotificationReceivedEventV2() {
        super(NOTIFICATION_RECEIVED_EVENT_TYPE);
    }
}
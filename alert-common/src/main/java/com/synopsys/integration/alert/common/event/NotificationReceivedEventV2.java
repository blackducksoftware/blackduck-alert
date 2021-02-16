package com.synopsys.integration.alert.common.event;

public class NotificationReceivedEventV2 extends AlertEvent {
    public static final String NOTIFICATION_RECEIVED_EVENT_TYPE = "notification_received_eventV2";

    public NotificationReceivedEventV2() {
        super(NOTIFICATION_RECEIVED_EVENT_TYPE);
    }
}
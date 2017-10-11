package com.blackducksoftware.integration.hub.notification.channel.hipchat;

import com.blackducksoftware.integration.hub.notification.datasource.entity.event.NotificationEntity;
import com.blackducksoftware.integration.hub.notification.event.AbstractChannelEvent;

public class HipChatEvent extends AbstractChannelEvent {

    public HipChatEvent(final NotificationEntity notificationEntity) {
        super(notificationEntity);
    }

    @Override
    public String getTopic() {
        return HipChatChannelConfig.CHANNEL_NAME;
    }
}

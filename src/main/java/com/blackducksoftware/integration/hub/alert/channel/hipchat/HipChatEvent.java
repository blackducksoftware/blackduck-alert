package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class HipChatEvent extends AbstractChannelEvent {

    public HipChatEvent(final NotificationEntity notificationEntity) {
        super(notificationEntity);
    }

    @Override
    public String getTopic() {
        return HipChatChannelConfig.CHANNEL_NAME;
    }
}

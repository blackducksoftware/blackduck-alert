package com.blackducksoftware.integration.hub.notification.channel.email;

import com.blackducksoftware.integration.hub.notification.datasource.entity.event.NotificationEntity;
import com.blackducksoftware.integration.hub.notification.event.AbstractChannelEvent;

public class EmailEvent extends AbstractChannelEvent {

    public EmailEvent(final NotificationEntity notificationEntity) {
        super(notificationEntity);
    }

    @Override
    public String getTopic() {
        return EmailChannelConfig.CHANNEL_NAME;
    }
}

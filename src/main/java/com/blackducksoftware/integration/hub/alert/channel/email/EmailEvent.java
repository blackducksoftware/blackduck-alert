package com.blackducksoftware.integration.hub.alert.channel.email;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;

public class EmailEvent extends AbstractChannelEvent {

    public EmailEvent(final NotificationEntity notificationEntity) {
        super(notificationEntity);
    }

    @Override
    public String getTopic() {
        return EmailChannelConfig.CHANNEL_NAME;
    }
}

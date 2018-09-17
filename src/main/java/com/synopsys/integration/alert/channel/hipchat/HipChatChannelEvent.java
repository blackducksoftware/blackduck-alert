package com.synopsys.integration.alert.channel.hipchat;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.event.ChannelEvent;

public class HipChatChannelEvent extends ChannelEvent {
    private final Integer roomId;
    private final Boolean notify;
    private final String color;

    public HipChatChannelEvent(final String createdAt, final String provider, final String notificationType, final String content, final Long notificationId, final Long commonConfigId, final Integer roomId, final Boolean notify,
        final String color) {
        super(EmailGroupChannel.COMPONENT_NAME, createdAt, provider, notificationType, content, notificationId, commonConfigId);
        this.roomId = roomId;
        this.notify = notify;
        this.color = color;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Boolean getNotify() {
        return notify;
    }

    public String getColor() {
        return color;
    }
}

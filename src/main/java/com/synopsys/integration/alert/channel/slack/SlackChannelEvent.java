package com.synopsys.integration.alert.channel.slack;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.event.ChannelEvent;

public class SlackChannelEvent extends ChannelEvent {
    private final String channelUsername;
    private final String webHook;
    private final String channelName;

    public SlackChannelEvent(final String createdAt, final String provider, final String notificationType, final String content, final Long notificationId, final Long commonConfigId, final String channelUsername, final String webHook,
        final String channelName) {
        super(EmailGroupChannel.COMPONENT_NAME, createdAt, provider, notificationType, content, notificationId, commonConfigId);
        this.channelUsername = channelUsername;
        this.webHook = webHook;
        this.channelName = channelName;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public String getWebHook() {
        return webHook;
    }

    public String getChannelName() {
        return channelName;
    }
}

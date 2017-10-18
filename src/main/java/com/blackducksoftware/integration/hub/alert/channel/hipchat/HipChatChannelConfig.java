package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import com.blackducksoftware.integration.hub.alert.channel.ChannelConfig;

public class HipChatChannelConfig extends ChannelConfig {
    public final static String CHANNEL_NAME = "hipchat_channel";

    public HipChatChannelConfig() {
        super(CHANNEL_NAME);
    }

}

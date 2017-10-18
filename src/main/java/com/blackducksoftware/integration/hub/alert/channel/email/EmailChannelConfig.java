package com.blackducksoftware.integration.hub.alert.channel.email;

import com.blackducksoftware.integration.hub.alert.channel.ChannelConfig;

public class EmailChannelConfig extends ChannelConfig {
    public final static String CHANNEL_NAME = "email_channel";

    public EmailChannelConfig() {
        super(CHANNEL_NAME);
    }
}

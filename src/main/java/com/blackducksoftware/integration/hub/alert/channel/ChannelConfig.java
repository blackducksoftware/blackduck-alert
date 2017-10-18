package com.blackducksoftware.integration.hub.alert.channel;

public class ChannelConfig {
    private final String channelName;

    public ChannelConfig(final String channelName) {
        this.channelName = channelName;

    }

    public String getChannelName() {
        return channelName;
    }
}

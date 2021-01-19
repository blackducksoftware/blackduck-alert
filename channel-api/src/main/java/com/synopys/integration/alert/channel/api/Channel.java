package com.synopys.integration.alert.channel.api;

public abstract class Channel {
    protected final ChannelMessageFormatter channelMessageFormatter;
    protected final ChannelMessageSender channelMessageSender;

    public Channel(ChannelMessageFormatter channelMessageFormatter, ChannelMessageSender channelMessageSender) {
        this.channelMessageFormatter = channelMessageFormatter;
        this.channelMessageSender = channelMessageSender;
    }

    public abstract void handleEvent(Object event);

}

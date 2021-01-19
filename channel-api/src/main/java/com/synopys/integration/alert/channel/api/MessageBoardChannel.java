package com.synopys.integration.alert.channel.api;

public abstract class MessageBoardChannel extends Channel {
    public MessageBoardChannel(ChannelMessageFormatter channelMessageFormatter, ChannelMessageSender channelMessageSender) {
        super(channelMessageFormatter, channelMessageSender);
    }

    @Override
    public final void handleEvent(Object event) {
        Object message = channelMessageFormatter.formatEvent(event);
        Object result = channelMessageSender.sendMessage(message);
    }

}

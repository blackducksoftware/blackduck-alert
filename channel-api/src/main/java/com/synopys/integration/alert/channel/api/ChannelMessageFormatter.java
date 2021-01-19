package com.synopys.integration.alert.channel.api;

public interface ChannelMessageFormatter {
    Object formatEvent(Object event);

}

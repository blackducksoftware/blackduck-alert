package com.synopsys.integration.alert.channel.msteams;

public class ClassNameChannelKey extends ChannelKey {
    public final String getUniversalKey() {
        return getClass().getSimpleName().toLowerCase();
    }

}

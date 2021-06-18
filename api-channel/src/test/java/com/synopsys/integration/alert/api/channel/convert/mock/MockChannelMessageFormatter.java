package com.synopsys.integration.alert.api.channel.convert.mock;

import com.synopsys.integration.alert.api.channel.convert.ChannelMessageFormatter;

public class MockChannelMessageFormatter extends ChannelMessageFormatter {
    public MockChannelMessageFormatter(int maxMessageLength) {
        super(maxMessageLength, System.lineSeparator());
    }

    @Override
    public String encode(String txt) {
        return "<b>" + txt + "</b>";
    }

    @Override
    public String emphasize(String txt) {
        return "<!>" + txt + "</!>";
    }

    @Override
    public String createLink(String txt, String url) {
        return "<ln>" + txt + " - " + url + "</ln>";
    }

}

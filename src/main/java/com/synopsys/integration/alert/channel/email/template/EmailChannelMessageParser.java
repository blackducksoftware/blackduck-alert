package com.synopsys.integration.alert.channel.email.template;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.ChannelMessageParser;

@Component
public class EmailChannelMessageParser extends ChannelMessageParser {
    @Override
    protected String encodeString(String txt) {
        // TODO is this required?
        return txt;
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("<strong>%s</strong>", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("<a href=\"%s\">%s</a>", url, txt);
    }

    @Override
    protected String getLineSeparator() {
        return "<br />";
    }

}

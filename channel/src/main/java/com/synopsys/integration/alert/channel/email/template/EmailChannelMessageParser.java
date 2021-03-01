/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.template;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.message.ChannelMessageParser;

@Component
public class EmailChannelMessageParser extends ChannelMessageParser {
    @Override
    protected String encodeString(String txt) {
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

    @Override
    protected String createMessageSeparator(String title) {
        return "";
    }

}

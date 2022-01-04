/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.convert.ChannelMessageFormatter;

@Component
public class EmailChannelMessageFormatter extends ChannelMessageFormatter {
    private static final int MAX_EMAIL_BODY_LENGTH = Integer.MAX_VALUE;
    private static final String EMAIL_LINE_SEPARATOR = "<br/>";
    private static final String EMAIL_NON_BREAKING_SPACE = "&nbsp;";

    public EmailChannelMessageFormatter() {
        super(MAX_EMAIL_BODY_LENGTH, EMAIL_LINE_SEPARATOR, ChannelMessageFormatter.DEFAULT_SECTION_SEPARATOR, EMAIL_NON_BREAKING_SPACE);
    }

    @Override
    public String encode(String txt) {
        return txt;
    }

    @Override
    public String emphasize(String txt) {
        return String.format("<strong>%s</strong>", txt);
    }

    @Override
    public String createLink(String txt, String url) {
        return String.format("<a href=\"%s\">%s</a>", url, txt);
    }

}

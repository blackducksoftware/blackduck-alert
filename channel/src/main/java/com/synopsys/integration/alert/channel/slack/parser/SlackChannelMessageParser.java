/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.slack.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.message.ChannelMessageParser;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;

@Component
public class SlackChannelMessageParser extends ChannelMessageParser {
    private static final Map<Character, String> SLACK_CHARACTER_ENCODING_MAP;

    private MarkupEncoderUtil markupEncoderUtil;

    static {
        // Insertion order matters, so '&' must always be inserted first.
        SLACK_CHARACTER_ENCODING_MAP = new LinkedHashMap<>();
        SLACK_CHARACTER_ENCODING_MAP.put('&', "&amp;");
        SLACK_CHARACTER_ENCODING_MAP.put('<', "&lt;");
        SLACK_CHARACTER_ENCODING_MAP.put('>', "&gt;");
    }

    @Autowired
    public SlackChannelMessageParser(MarkupEncoderUtil markupEncoderUtil) {
        this.markupEncoderUtil = markupEncoderUtil;
    }

    @Override
    protected String encodeString(String txt) {
        return markupEncoderUtil.encodeMarkup(SLACK_CHARACTER_ENCODING_MAP, txt);
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("*%s*", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("<%s|%s>", url, txt);
    }

    @Override
    protected String getLineSeparator() {
        return "\n";
    }

}

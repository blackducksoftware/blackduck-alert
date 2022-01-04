/*
 * channel-msteams
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.distribution;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.convert.ChannelMessageFormatter;
import com.synopsys.integration.alert.common.util.MarkupEncoderUtil;

@Component
public class MSTeamsChannelMessageFormatter extends ChannelMessageFormatter {
    // There is a size limit in the request size that is allowed (20KB). This text limit is meant to hopefully keep the message under that size limit
    private static final int MAX_MSTEAMS_MESSAGE_LENGTH = 10000;
    private static final String MSTEAMS_LINE_SEPARATOR = "\r\n\r\n";
    private static final Map<Character, String> MSTEAMS_CHARACTER_ENCODING_MAP = Map.of(
        '*', "\\*",
        '~', "\\~",
        '#', "\\#",
        '-', "\\-",
        '_', "\\_"
    );

    private final MarkupEncoderUtil markupEncoderUtil;

    @Autowired
    public MSTeamsChannelMessageFormatter(MarkupEncoderUtil markupEncoderUtil) {
        super(MAX_MSTEAMS_MESSAGE_LENGTH, MSTEAMS_LINE_SEPARATOR);
        this.markupEncoderUtil = markupEncoderUtil;
    }

    @Override
    public String encode(String txt) {
        return markupEncoderUtil.encodeMarkup(MSTEAMS_CHARACTER_ENCODING_MAP, txt);
    }

    @Override
    public String emphasize(String txt) {
        return String.format("**%s**", txt);
    }

    @Override
    public String createLink(String txt, String url) {
        return String.format("[%s](%s)", txt, url);
    }

}

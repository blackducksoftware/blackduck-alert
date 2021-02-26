/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.service;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageParser;

@Component
public class AzureBoardsMessageParser extends IssueTrackerMessageParser {
    public static final int TITLE_SIZE_LIMIT = 255;
    public static final int MESSAGE_SIZE_LIMIT = 30000;

    public AzureBoardsMessageParser() {
        super(TITLE_SIZE_LIMIT, MESSAGE_SIZE_LIMIT);
    }

    @Override
    protected String encodeString(String txt) {
        return txt;
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("<b>%s</b>", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        return String.format("<a href=\"%s\">%s</a>", url, txt);
    }

    @Override
    protected String getLineSeparator() {
        return "<br>";
    }

}

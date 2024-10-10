/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.azure.boards.distribution;

import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.convert.IssueTrackerMessageFormatter;

@Component
public class AzureBoardsMessageFormatter extends IssueTrackerMessageFormatter {
    private static final int TITLE_SIZE_LIMIT = 255;
    private static final int MESSAGE_SIZE_LIMIT = 30000;
    private static final String LINE_SEPARATOR = "<br>";
    private static final String SECTION_SEPARATOR = "<hr>";
    private static final String NON_BREAKING_SPACE = "&nbsp;";

    public AzureBoardsMessageFormatter() {
        super(TITLE_SIZE_LIMIT, MESSAGE_SIZE_LIMIT, MESSAGE_SIZE_LIMIT, LINE_SEPARATOR, SECTION_SEPARATOR, NON_BREAKING_SPACE);
    }

    @Override
    public String encode(String txt) {
        return txt;
    }

    @Override
    public String emphasize(String txt) {
        return String.format("<b>%s</b>", txt);
    }

    @Override
    public String createLink(String txt, String url) {
        return String.format("<a href=\"%s\">%s</a>", url, txt);
    }

}

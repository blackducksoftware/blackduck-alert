/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerMessageParser;

@Component
public class JiraMessageParser extends IssueTrackerMessageParser {
    private static final int TITLE_SIZE_LIMIT = 255;
    private static final int MESSAGE_SIZE_LIMIT = 30000;

    @Autowired
    public JiraMessageParser() {
        super(TITLE_SIZE_LIMIT, MESSAGE_SIZE_LIMIT);
    }

    @Override
    protected String encodeString(String txt) {
        return txt;
    }

    @Override
    protected String emphasize(String txt) {
        return String.format("*%s*", txt);
    }

    @Override
    protected String createLink(String txt, String url) {
        // Jira uses [] for link content the trailing ] is removed on Jira Server; add whitespace before and after the brackets to fix and be consistent for Jira Server and Jira Cloud.
        return String.format(" [%s|%s] ", txt, url);
    }

    @Override
    protected String getLineSeparator() {
        return "\n";
    }

    @Override
    protected String getSectionSeparator() {
        return "";
    }

}

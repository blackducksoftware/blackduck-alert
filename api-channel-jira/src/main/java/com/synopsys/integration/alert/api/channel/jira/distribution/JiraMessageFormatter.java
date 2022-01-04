/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.jira.distribution;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.issue.convert.IssueTrackerMessageFormatter;

@Component
public class JiraMessageFormatter extends IssueTrackerMessageFormatter {
    private static final int TITLE_SIZE_LIMIT = 254;
    private static final int MESSAGE_SIZE_LIMIT = 30000;

    public JiraMessageFormatter() {
        super(TITLE_SIZE_LIMIT, MESSAGE_SIZE_LIMIT, MESSAGE_SIZE_LIMIT, "\n");
    }

    @Override
    public String encode(String txt) {
        return txt;
    }

    @Override
    public String emphasize(String txt) {
        return String.format("*%s*", txt);
    }

    @Override
    public String createLink(String txt, String url) {
        // Jira uses [] for link content the trailing ] is removed on Jira Server; add whitespace before and after the brackets to fix and be consistent for Jira Server and Jira Cloud.
        return String.format("%s[%s|%s]%s", getNonBreakingSpace(), txt, url, getNonBreakingSpace());
    }

}

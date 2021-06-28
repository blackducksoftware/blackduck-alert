package com.synopsys.integration.alert.api.channel.issue.convert.mock;

import com.synopsys.integration.alert.api.channel.issue.convert.IssueTrackerMessageFormatter;

public class MockIssueTrackerMessageFormatter extends IssueTrackerMessageFormatter {
    public static MockIssueTrackerMessageFormatter withIntegerMaxValueLength() {
        return new MockIssueTrackerMessageFormatter(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public MockIssueTrackerMessageFormatter(int maxTitleLength, int maxDescriptionLength, int maxCommentLength) {
        super(maxTitleLength, maxDescriptionLength, maxCommentLength, System.lineSeparator());
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

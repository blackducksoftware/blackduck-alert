package com.synopsys.integration.alert.issuetracker.jira.common.util;

import com.synopsys.integration.alert.issuetracker.message.IssueContentLengthValidator;

public class JiraContentValidator extends IssueContentLengthValidator {
    private static final int CONTENT_LENGTH = 30000;

    @Override
    public int getTitleLength() {
        return 255;
    }

    @Override
    public int getDescriptionLength() {
        return CONTENT_LENGTH;
    }

    @Override
    public int getCommentLength() {
        return CONTENT_LENGTH;
    }
}

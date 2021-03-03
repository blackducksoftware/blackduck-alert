/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.common.util;

import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueContentLengthValidator;

// TODO remove this unnecessary abstraction
public class JiraContentValidator extends IssueContentLengthValidator {
    public static final int CONTENT_LENGTH = 30000;
    public static final int TITLE_LENGTH = 255;

    public JiraContentValidator() {
        super(TITLE_LENGTH, CONTENT_LENGTH, CONTENT_LENGTH);
    }

}

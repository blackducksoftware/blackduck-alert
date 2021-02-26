/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.exception;

public class IssueTrackerContentLengthException extends IssueTrackerException {
    private static final long serialVersionUID = -796650409951066155L;

    public IssueTrackerContentLengthException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IssueTrackerContentLengthException(String message, Throwable cause) {
        super(message, cause);
    }

    public IssueTrackerContentLengthException(String message) {
        super(message);
    }

    public IssueTrackerContentLengthException(Throwable cause) {
        super(cause);
    }

    public IssueTrackerContentLengthException() {
    }
}

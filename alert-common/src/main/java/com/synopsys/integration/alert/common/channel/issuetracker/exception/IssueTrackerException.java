/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.exception;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;

public class IssueTrackerException extends AlertException {
    private static final long serialVersionUID = -156290045811635478L;

    public IssueTrackerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IssueTrackerException(String message, Throwable cause) {
        super(message, cause);
    }

    public IssueTrackerException(String message) {
        super(message);
    }

    public IssueTrackerException(Throwable cause) {
        super(cause);
    }

    public IssueTrackerException() {
    }

}

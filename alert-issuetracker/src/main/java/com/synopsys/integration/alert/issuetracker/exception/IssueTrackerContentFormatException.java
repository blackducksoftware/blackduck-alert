package com.synopsys.integration.alert.issuetracker.exception;

public class IssueTrackerContentFormatException extends IssueTrackerException {
    private static final long serialVersionUID = -796650409951066155L;

    public IssueTrackerContentFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IssueTrackerContentFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IssueTrackerContentFormatException(String message) {
        super(message);
    }

    public IssueTrackerContentFormatException(Throwable cause) {
        super(cause);
    }

    public IssueTrackerContentFormatException() {
    }
}

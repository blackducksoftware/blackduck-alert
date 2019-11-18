package com.synopsys.integration.alert.issuetracker.message;

import com.synopsys.integration.alert.issuetracker.IssueProperties;
import com.synopsys.integration.alert.issuetracker.OperationType;

public class IssueCommentRequest extends IssueTrackerRequest {

    private IssueCommentRequest(OperationType operation,
        IssueProperties issueProperties, IssueContentModel requestContent) {
        super(operation, issueProperties, requestContent);
    }

    public static final IssueCommentRequest of(IssueProperties issueProperties, IssueContentModel content) {
        return new IssueCommentRequest(OperationType.UPDATE, issueProperties, content);
    }
}

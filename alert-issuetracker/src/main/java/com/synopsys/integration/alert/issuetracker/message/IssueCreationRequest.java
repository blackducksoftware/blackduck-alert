package com.synopsys.integration.alert.issuetracker.message;

import com.synopsys.integration.alert.issuetracker.IssueProperties;
import com.synopsys.integration.alert.issuetracker.OperationType;

public class IssueCreationRequest extends IssueTrackerRequest {

    private IssueCreationRequest(OperationType operation,
        IssueProperties issueProperties, IssueContentModel requestContent) {
        super(operation, issueProperties, requestContent);
    }

    public static final IssueCreationRequest of(IssueProperties issueProperties, IssueContentModel content) {
        return new IssueCreationRequest(OperationType.CREATE, issueProperties, content);
    }
}

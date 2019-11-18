package com.synopsys.integration.alert.issuetracker.message;

import com.synopsys.integration.alert.issuetracker.IssueContentModel;
import com.synopsys.integration.alert.issuetracker.IssueProperties;
import com.synopsys.integration.alert.issuetracker.OperationType;

public class IssueResolutionRequest extends IssueTrackerRequest {

    private IssueResolutionRequest(OperationType operation,
        IssueProperties issueProperties, IssueContentModel requestContent) {
        super(operation, issueProperties, requestContent);
    }

    public static final IssueResolutionRequest of(IssueProperties issueProperties, IssueContentModel content) {
        return new IssueResolutionRequest(OperationType.RESOLVE, issueProperties, content);
    }
}

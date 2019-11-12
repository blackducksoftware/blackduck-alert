package com.synopsys.integration.alert.issuetracker.message;

import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.issuetracker.IssueTrackerContext;

public class IssueTrackerRequest {
    private IssueTrackerContext context;
    private MessageContentGroup requestContent;

    public IssueTrackerRequest(IssueTrackerContext context, MessageContentGroup requestContent) {
        this.context = context;
        this.requestContent = requestContent;
    }

    public IssueTrackerContext getContext() {
        return context;
    }

    public MessageContentGroup getRequestContent() {
        return requestContent;
    }
}

package com.synopsys.integration.alert.channel.jira.cloud.distribution;

import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueTransitionRetriever;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public class JiraCloudIssueTransitionRetriever implements JiraIssueTransitionRetriever {
    private final IssueService issueService;

    public JiraCloudIssueTransitionRetriever(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException {
        return issueService.getTransitions(issueKey);
    }
}

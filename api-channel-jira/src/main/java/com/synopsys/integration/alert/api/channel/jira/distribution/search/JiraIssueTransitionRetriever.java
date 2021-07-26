package com.synopsys.integration.alert.api.channel.jira.distribution.search;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;

public interface JiraIssueTransitionRetriever {
    TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException;
}

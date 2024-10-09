package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.response.TransitionsResponseModel;

@FunctionalInterface
public interface JiraIssueTransitionRetriever {
    TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException;
}

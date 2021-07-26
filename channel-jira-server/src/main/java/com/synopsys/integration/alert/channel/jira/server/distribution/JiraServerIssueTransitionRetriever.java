/*
 * channel-jira-server
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution;

import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueTransitionRetriever;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.response.TransitionsResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueService;

public class JiraServerIssueTransitionRetriever implements JiraIssueTransitionRetriever {
    private final IssueService issueService;

    public JiraServerIssueTransitionRetriever(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public TransitionsResponseModel fetchIssueTransitions(String issueKey) throws IntegrationException {
        return issueService.getTransitions(issueKey);
    }
}

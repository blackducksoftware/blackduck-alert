/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.util;

import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.util.JiraIssuePropertyHandler;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;

public class JiraCloudIssuePropertyHandler extends JiraIssuePropertyHandler<IssueSearchResponseModel> {
    private final IssueSearchService issueSearchService;
    private final IssuePropertyService issuePropertyService;

    public JiraCloudIssuePropertyHandler(IssueSearchService issueSearchService, IssuePropertyService issuePropertyService) {
        this.issueSearchService = issueSearchService;
        this.issuePropertyService = issuePropertyService;
    }

    @Override
    public IssueSearchResponseModel queryForIssues(String query) throws IntegrationException {
        return issueSearchService.queryForIssues(query);
    }

    @Override
    public void addPropertiesToIssue(String issueKey, JiraIssueSearchProperties properties) throws IntegrationException {
        issuePropertyService.setProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, properties);
    }

}

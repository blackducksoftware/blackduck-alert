/*
 * api-channel-jira
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.distribution.search;

import com.blackduck.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.response.IssuePropertyResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.google.gson.Gson;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;

public class JiraIssueAlertPropertiesManager {
    private final Gson gson;
    private final IssuePropertyService issuePropertyService;

    public JiraIssueAlertPropertiesManager(Gson gson, IssuePropertyService issuePropertyService) {
        this.gson = gson;
        this.issuePropertyService = issuePropertyService;
    }

    public JiraIssueSearchProperties retrieveIssueProperties(String jiraIssueIdOrKey) throws AlertException {
        try {
            IssuePropertyResponseModel response = issuePropertyService.getProperty(jiraIssueIdOrKey, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_KEY);
            return gson.fromJson(response.getValue(), JiraIssueSearchProperties.class);
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Could not retrieve issue properties for issue [%s]", jiraIssueIdOrKey), e);
        }
    }

    public void assignIssueProperties(String jiraIssueIdOrKey, JiraIssueSearchProperties properties) throws AlertException {
        try {
            issuePropertyService.setProperty(jiraIssueIdOrKey, JiraIssuePropertyKeys.JIRA_ISSUE_PROPERTY_KEY, properties);
        } catch (IntegrationException e) {
            throw new AlertException(String.format("Could not assign issue properties for issue [%s]", jiraIssueIdOrKey), e);
        }
    }

}

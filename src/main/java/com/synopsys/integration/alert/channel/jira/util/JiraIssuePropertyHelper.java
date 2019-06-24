package com.synopsys.integration.alert.channel.jira.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.JiraConstants;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.response.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueSearchService;

public class JiraIssuePropertyHelper {
    private final Gson gson;
    private final IssueSearchService issueSearchService;

    public JiraIssuePropertyHelper(Gson gson, IssueSearchService issueSearchService) {
        this.gson = gson;
        this.issueSearchService = issueSearchService;
    }

    public Optional<IssueSearchResponseModel> findIssues(String category, String bomComponentUri) throws IntegrationException {
        return findIssues(category, bomComponentUri, null);
    }

    public Optional<IssueSearchResponseModel> findIssues(String category, String bomComponentUri, String policyName) throws IntegrationException {
        final StringBuilder jqlBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(category)) {
            jqlBuilder.append(createPropertySearchString(JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY, category));
            jqlBuilder.append(StringUtils.SPACE);
        }
        if (StringUtils.isNotBlank(bomComponentUri)) {
            jqlBuilder.append(StringUtils.SPACE);
            jqlBuilder.append(createPropertySearchString(JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_BOM_COMPONENT_URI, bomComponentUri));
        }
        if (StringUtils.isNotBlank(policyName)) {
            jqlBuilder.append(StringUtils.SPACE);
            jqlBuilder.append(createPropertySearchString(JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_POLICY_NAME, policyName));
        }

        final String jql = jqlBuilder.toString();
        if (!jql.isBlank()) {
            final IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssues(jql);
            return Optional.of(issueSearchResponseModel);
        }
        return Optional.empty();
    }

    public void addPropertiesToIssue(String issueKey, String category, String bomComponentUri) {
        addPropertiesToIssue(issueKey, null, bomComponentUri);
    }

    public void addPropertiesToIssue(String issueKey, String category, String bomComponentUri, String policyName) {
        // TODO implement
    }

    private String createPropertySearchString(String key, String value) {
        final String propertySearchFormat = "issue.property[%s].%s ~ %s";
        return String.format(propertySearchFormat, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, key, value);
    }

}

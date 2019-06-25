/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.jira.util;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.jira.JiraConstants;
import com.synopsys.integration.alert.channel.jira.model.AlertJiraIssueProperties;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.model.response.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.cloud.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.cloud.rest.service.IssueSearchService;

public class JiraIssuePropertyHelper {
    private final IssueSearchService issueSearchService;
    private final IssuePropertyService issuePropertyService;

    public JiraIssuePropertyHelper(IssueSearchService issueSearchService, IssuePropertyService issuePropertyService) {
        this.issueSearchService = issueSearchService;
        this.issuePropertyService = issuePropertyService;
    }

    public Optional<IssueSearchResponseModel> findIssues(String category, String uniqueId) throws IntegrationException {
        final StringBuilder jqlBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(category)) {
            jqlBuilder.append(createPropertySearchString(JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_CATEGORY, category));
            jqlBuilder.append(StringUtils.SPACE);
        }
        if (StringUtils.isNotBlank(uniqueId)) {
            jqlBuilder.append(createPropertySearchString(JiraConstants.JIRA_ISSUE_PROPERTY_OBJECT_KEY_UNIQUE_ID, uniqueId));
        }

        final String jql = jqlBuilder.toString();
        if (!jql.isBlank()) {
            final IssueSearchResponseModel issueSearchResponseModel = issueSearchService.queryForIssues(jql);
            return Optional.of(issueSearchResponseModel);
        }
        return Optional.empty();
    }

    public void addPropertiesToIssue(String issueKey, String category, String uniqueId) throws IntegrationException {
        AlertJiraIssueProperties properties = new AlertJiraIssueProperties(category, uniqueId);
        addPropertiesToIssue(issueKey, properties);
    }

    public void addPropertiesToIssue(String issueKey, AlertJiraIssueProperties properties) throws IntegrationException {
        issuePropertyService.setProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, properties);
    }

    private String createPropertySearchString(String key, String value) {
        final String propertySearchFormat = "issue.property[%s].%s ~ '%s'";
        return String.format(propertySearchFormat, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, key, value);
    }

}

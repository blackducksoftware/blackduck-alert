/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira2.cloud;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira2.common.JiraIssuePropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.response.IssuePropertyResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;

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

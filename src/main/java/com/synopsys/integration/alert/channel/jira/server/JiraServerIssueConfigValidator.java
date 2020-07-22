/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.jira.server;

import java.util.Collection;

import com.synopsys.integration.alert.channel.jira.common.JiraIssueConfigValidator;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;
import com.synopsys.integration.jira.common.server.service.ProjectService;
import com.synopsys.integration.jira.common.server.service.UserSearchService;

public class JiraServerIssueConfigValidator extends JiraIssueConfigValidator {
    private final ProjectService projectService;
    private final UserSearchService userSearchService;

    public JiraServerIssueConfigValidator(ProjectService projectService, UserSearchService userSearchService, IssueTypeService issueTypeService, IssueMetaDataService issueMetaDataService) {
        super(issueTypeService, issueMetaDataService);
        this.projectService = projectService;
        this.userSearchService = userSearchService;
    }

    @Override
    public String getProjectFieldKey() {
        return JiraServerProperties.KEY_JIRA_PROJECT_NAME;
    }

    @Override
    public String getIssueTypeFieldKey() {
        return JiraServerProperties.KEY_ISSUE_TYPE;
    }

    @Override
    public String getIssueCreatorFieldKey() {
        return JiraServerProperties.KEY_ISSUE_CREATOR;
    }

    @Override
    public String getAddCommentsFieldKey() {
        return JiraServerProperties.KEY_ADD_COMMENTS;
    }

    @Override
    public String getResolveTransitionFieldKey() {
        return JiraServerProperties.KEY_RESOLVE_WORKFLOW_TRANSITION;
    }

    @Override
    public String getOpenTransitionFieldKey() {
        return JiraServerProperties.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    public Collection<ProjectComponent> getProjectsByName(String jiraProjectName) throws IntegrationException {
        return projectService.getProjectsByName(jiraProjectName);
    }

    @Override
    public boolean isUserValid(String issueCreator) throws IntegrationException {
        return userSearchService.findUserByUsername(issueCreator)
                   .map(UserDetailsResponseModel::getName)
                   .filter(name -> name.equals(issueCreator))
                   .isPresent();
    }
}

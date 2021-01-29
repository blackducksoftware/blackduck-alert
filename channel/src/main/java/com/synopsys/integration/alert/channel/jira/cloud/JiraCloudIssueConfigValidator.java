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
package com.synopsys.integration.alert.channel.jira.cloud;

import java.util.Collection;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraCloudDescriptor;
import com.synopsys.integration.alert.channel.jira.common.JiraIssueConfigValidator;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.cloud.service.UserSearchService;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.response.PageOfProjectsResponseModel;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;

public class JiraCloudIssueConfigValidator extends JiraIssueConfigValidator {
    private final ProjectService projectService;
    private final UserSearchService userSearchService;

    public JiraCloudIssueConfigValidator(ProjectService projectService, UserSearchService userSearchService, IssueTypeService issueTypeService, IssueMetaDataService issueMetaDataService) {
        super(issueTypeService, issueMetaDataService);
        this.projectService = projectService;
        this.userSearchService = userSearchService;
    }

    @Override
    public String getProjectFieldKey() {
        return JiraCloudDescriptor.KEY_JIRA_PROJECT_NAME;
    }

    @Override
    public String getIssueTypeFieldKey() {
        return JiraCloudDescriptor.KEY_ISSUE_TYPE;
    }

    @Override
    public String getIssueCreatorFieldKey() {
        return JiraCloudDescriptor.KEY_ISSUE_CREATOR;
    }

    @Override
    public String getAddCommentsFieldKey() {
        return JiraCloudDescriptor.KEY_ADD_COMMENTS;
    }

    @Override
    public String getResolveTransitionFieldKey() {
        return JiraCloudDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION;
    }

    @Override
    public String getOpenTransitionFieldKey() {
        return JiraCloudDescriptor.KEY_OPEN_WORKFLOW_TRANSITION;
    }

    @Override
    public Collection<ProjectComponent> getProjectsByName(String jiraProjectName) throws IntegrationException {
        PageOfProjectsResponseModel projectsResponseModel = projectService.getProjectsByName(jiraProjectName);
        return projectsResponseModel.getProjects();
    }

    @Override
    public boolean isUserValid(String issueCreator) throws IntegrationException {
        return userSearchService.findUser(issueCreator).stream()
                   .map(UserDetailsResponseModel::getEmailAddress)
                   .anyMatch(name -> name.equals(issueCreator));
    }
}

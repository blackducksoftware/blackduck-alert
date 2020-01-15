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
package com.synopsys.integration.alert.channel.jira.cloud;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraDescriptor;
import com.synopsys.integration.alert.channel.jira.cloud.descriptor.JiraDistributionUIConfig;
import com.synopsys.integration.alert.channel.jira.common.JiraConstants;
import com.synopsys.integration.alert.common.channel.issuetracker.IssueConfig;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.cloud.service.UserSearchService;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.model.response.PageOfProjectsResponseModel;
import com.synopsys.integration.jira.common.model.response.UserDetailsResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;

public class JiraIssueConfigValidator {
    private static final String CONNECTION_ERROR_FORMAT_STRING = "There was a problem getting the %s from Jira. Please ensure the server is configured correctly.";

    private final ProjectService projectService;
    private final UserSearchService userSearchService;
    private final IssueTypeService issueTypeService;
    private final IssueMetaDataService issueMetaDataService;

    public JiraIssueConfigValidator(ProjectService projectService, UserSearchService userSearchService, IssueTypeService issueTypeService, IssueMetaDataService issueMetaDataService) {
        this.projectService = projectService;
        this.userSearchService = userSearchService;
        this.issueTypeService = issueTypeService;
        this.issueMetaDataService = issueMetaDataService;
    }

    public IssueConfig validate(FieldAccessor fieldAccessor) throws AlertFieldException {
        IssueConfig jiraIssueConfig = new IssueConfig();
        Map<String, String> fieldErrors = new HashMap<>();

        ProjectComponent projectComponent = validateProject(fieldAccessor, fieldErrors);
        if (projectComponent != null) {
            jiraIssueConfig.setProjectId(projectComponent.getId());
            jiraIssueConfig.setProjectKey(projectComponent.getKey());
            jiraIssueConfig.setProjectName(projectComponent.getName());
        }

        String issueCreator = validateIssueCreator(fieldAccessor, fieldErrors);
        jiraIssueConfig.setIssueCreator(issueCreator);

        String issueType = validateIssueType(fieldAccessor, fieldErrors);
        jiraIssueConfig.setIssueType(issueType);

        Boolean commentOnIssue = fieldAccessor.getBooleanOrFalse(JiraDescriptor.KEY_ADD_COMMENTS);
        jiraIssueConfig.setCommentOnIssues(commentOnIssue);

        String resolveTransition = fieldAccessor.getStringOrNull(JiraDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION);
        jiraIssueConfig.setResolveTransition(resolveTransition);

        String openTransition = fieldAccessor.getStringOrNull(JiraDescriptor.KEY_OPEN_WORKFLOW_TRANSITION);
        jiraIssueConfig.setOpenTransition(openTransition);

        if (fieldErrors.isEmpty()) {
            return jiraIssueConfig;
        } else {
            throw new AlertFieldException(JiraConstants.JIRA_ISSUE_VALIDATION_ERROR_MESSAGE, fieldErrors);
        }
    }

    private ProjectComponent validateProject(FieldAccessor fieldAccessor, Map<String, String> fieldErrors) {
        Optional<String> optionalProjectName = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_PROJECT_NAME);
        if (optionalProjectName.isPresent()) {
            String jiraProjectName = optionalProjectName.get();
            try {
                PageOfProjectsResponseModel projectsResponseModel = projectService.getProjectsByName(jiraProjectName);
                Optional<ProjectComponent> optionalProject = projectsResponseModel.getProjects()
                                                                 .stream()
                                                                 .filter(project -> jiraProjectName.equals(project.getName()) || jiraProjectName.equals(project.getKey()))
                                                                 .findAny();
                if (optionalProject.isPresent()) {
                    return optionalProject.get();
                } else {
                    fieldErrors.put(JiraDescriptor.KEY_JIRA_PROJECT_NAME, String.format("No project named '%s' was found", jiraProjectName));
                }
            } catch (IntegrationException e) {
                fieldErrors.put(JiraDescriptor.KEY_JIRA_PROJECT_NAME, String.format(CONNECTION_ERROR_FORMAT_STRING, "projects"));
            }
        } else {
            requireField(fieldErrors, JiraDescriptor.KEY_JIRA_PROJECT_NAME);
        }
        return null;
    }

    private String validateIssueCreator(FieldAccessor fieldAccessor, Map<String, String> fieldErrors) {
        Optional<String> optionalIssueCreator = fieldAccessor.getString(JiraDescriptor.KEY_ISSUE_CREATOR)
                                                    .filter(StringUtils::isNotBlank)
                                                    .or(() -> fieldAccessor.getString(JiraDescriptor.KEY_JIRA_ADMIN_EMAIL_ADDRESS));
        if (optionalIssueCreator.isPresent()) {
            String issueCreator = optionalIssueCreator.get();
            try {
                boolean isValidJiraEmail = userSearchService.findUser(issueCreator)
                                               .stream()
                                               .map(UserDetailsResponseModel::getEmailAddress)
                                               .anyMatch(emailAddress -> emailAddress.equals(issueCreator));
                if (isValidJiraEmail) {
                    return issueCreator;
                } else {
                    fieldErrors.put(JiraDescriptor.KEY_ISSUE_CREATOR, String.format("The email address '%s' is not associated with any valid Jira users.", issueCreator));
                }
            } catch (IntegrationException e) {
                fieldErrors.put(JiraDescriptor.KEY_ISSUE_CREATOR, String.format(CONNECTION_ERROR_FORMAT_STRING, "users"));
            }
        } else {
            requireField(fieldErrors, JiraDescriptor.KEY_ISSUE_CREATOR);
        }
        return null;
    }

    private String validateIssueType(FieldAccessor fieldAccessor, Map<String, String> fieldErrors) {
        Optional<String> optionalProjectName = fieldAccessor.getString(JiraDescriptor.KEY_JIRA_PROJECT_NAME);
        String issueType = fieldAccessor.getString(JiraDescriptor.KEY_ISSUE_TYPE).orElse(JiraDistributionUIConfig.DEFAULT_ISSUE_TYPE);
        try {
            boolean isValidIssueType = issueTypeService.getAllIssueTypes()
                                           .stream()
                                           .map(IssueTypeResponseModel::getName)
                                           .anyMatch(issueType::equals);
            if (isValidIssueType) {
                if (optionalProjectName.isPresent()) {
                    String projectName = optionalProjectName.get();
                    boolean isValidForProject = issueMetaDataService.doesProjectContainIssueType(projectName, issueType);
                    if (isValidForProject) {
                        return issueType;
                    } else {
                        fieldErrors.put(JiraDescriptor.KEY_ISSUE_TYPE, String.format("The issue type '%s' not assigned to project '%s'", issueType, projectName));
                    }
                }
            } else {
                fieldErrors.put(JiraDescriptor.KEY_ISSUE_TYPE, String.format("The issue type '%s' could not be found", issueType));
            }
        } catch (IntegrationException e) {
            fieldErrors.put(JiraDescriptor.KEY_ISSUE_TYPE, String.format(CONNECTION_ERROR_FORMAT_STRING, "issue types"));
        }
        return null;
    }

    private void requireField(Map<String, String> fieldErrors, String key) {
        fieldErrors.put(key, "This field is required");
    }

}

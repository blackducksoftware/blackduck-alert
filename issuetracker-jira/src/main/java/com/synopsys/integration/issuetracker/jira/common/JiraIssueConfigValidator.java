/**
 * issuetracker-jira
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
package com.synopsys.integration.issuetracker.jira.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.channel.issuetracker.exception.IssueTrackerFieldException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.ProjectComponent;
import com.synopsys.integration.jira.common.model.response.IssueTypeResponseModel;
import com.synopsys.integration.jira.common.rest.service.IssueMetaDataService;
import com.synopsys.integration.jira.common.rest.service.IssueTypeService;

public abstract class JiraIssueConfigValidator {
    private static final String CONNECTION_ERROR_FORMAT_STRING = "There was a problem getting the %s from Jira. Please ensure the server is configured correctly.";

    private final IssueTypeService issueTypeService;
    private final IssueMetaDataService issueMetaDataService;

    public JiraIssueConfigValidator(IssueTypeService issueTypeService, IssueMetaDataService issueMetaDataService) {
        this.issueTypeService = issueTypeService;
        this.issueMetaDataService = issueMetaDataService;
    }

    public abstract String getProjectFieldKey();

    public abstract String getIssueTypeFieldKey();

    public abstract String getIssueCreatorFieldKey();

    public abstract String getAddCommentsFieldKey();

    public abstract String getResolveTransitionFieldKey();

    public abstract String getOpenTransitionFieldKey();

    public abstract Collection<ProjectComponent> getProjectsByName(String jiraProjectName) throws IntegrationException;

    public abstract boolean isUserValid(String issueCreator) throws IntegrationException;

    public IssueConfig createValidIssueConfig(IssueTrackerContext context) throws IssueTrackerFieldException {
        Map<String, String> fieldErrors = new HashMap<>();
        IssueConfig issueConfig = context.getIssueConfig();
        IssueConfig newConfig = new IssueConfig();
        newConfig.setCommentOnIssues(issueConfig.getCommentOnIssues());
        newConfig.setOpenTransition(issueConfig.getOpenTransition().orElse(null));
        newConfig.setResolveTransition(issueConfig.getResolveTransition().orElse(null));
        ProjectComponent projectComponent = validateProject(issueConfig, fieldErrors);
        if (projectComponent != null) {
            newConfig.setProjectId(projectComponent.getId());
            newConfig.setProjectKey(projectComponent.getKey());
            newConfig.setProjectName(projectComponent.getName());
        }

        newConfig.setIssueCreator(validateIssueCreator(issueConfig, fieldErrors));
        newConfig.setIssueType(validateIssueType(issueConfig, fieldErrors));

        if (!fieldErrors.isEmpty()) {
            throw new IssueTrackerFieldException(JiraConstants.JIRA_ISSUE_VALIDATION_ERROR_MESSAGE, fieldErrors);
        }

        return newConfig;
    }

    private ProjectComponent validateProject(IssueConfig config, Map<String, String> fieldErrors) {
        String jiraProjectName = config.getProjectName();
        if (StringUtils.isNotBlank(jiraProjectName)) {
            try {
                Collection<ProjectComponent> projectsResponseModel = getProjectsByName(jiraProjectName);
                Optional<ProjectComponent> optionalProject = projectsResponseModel
                                                                 .stream()
                                                                 .filter(project -> jiraProjectName.equals(project.getName()) || jiraProjectName.equals(project.getKey()))
                                                                 .findAny();
                if (optionalProject.isPresent()) {
                    return optionalProject.get();
                } else {
                    fieldErrors.put(getProjectFieldKey(), String.format("No project named '%s' was found", jiraProjectName));
                }
            } catch (IntegrationException e) {
                fieldErrors.put(getProjectFieldKey(), String.format(CONNECTION_ERROR_FORMAT_STRING, "projects"));
            }
        } else {
            requireField(fieldErrors, getProjectFieldKey());
        }
        return null;
    }

    private String validateIssueCreator(IssueConfig config, Map<String, String> fieldErrors) {
        String issueCreatorFieldKey = getIssueCreatorFieldKey();
        String issueCreator = config.getIssueCreator();
        try {
            if (StringUtils.isNotBlank(issueCreator) && isUserValid(issueCreator)) {
                return issueCreator;
            } else {
                fieldErrors.put(issueCreatorFieldKey, String.format("The username '%s' is not associated with any valid Jira users.", issueCreator));
            }
        } catch (IntegrationException e) {
            fieldErrors.put(issueCreatorFieldKey, String.format(CONNECTION_ERROR_FORMAT_STRING, "users"));
        }
        return null;
    }

    private String validateIssueType(IssueConfig config, Map<String, String> fieldErrors) {
        String issueTypeFieldKey = getIssueTypeFieldKey();
        String issueType = config.getIssueType();
        try {
            boolean isValidIssueType = issueTypeService.getAllIssueTypes()
                                           .stream()
                                           .map(IssueTypeResponseModel::getName)
                                           .anyMatch(issueType::equals);
            if (isValidIssueType) {
                String projectName = config.getProjectName();
                if (StringUtils.isNotBlank(projectName)) {
                    boolean isValidForProject = issueMetaDataService.doesProjectContainIssueType(projectName, issueType);
                    if (isValidForProject) {
                        return issueType;
                    } else {
                        fieldErrors.put(issueTypeFieldKey, String.format("The issue type '%s' not assigned to project '%s'", issueType, projectName));
                    }
                }
            } else {
                fieldErrors.put(issueTypeFieldKey, String.format("The issue type '%s' could not be found", issueType));
            }
        } catch (IntegrationException e) {
            fieldErrors.put(issueTypeFieldKey, String.format(CONNECTION_ERROR_FORMAT_STRING, "issue types"));
        }
        return null;
    }

    private void requireField(Map<String, String> fieldErrors, String key) {
        fieldErrors.put(key, "This field is required");
    }
}

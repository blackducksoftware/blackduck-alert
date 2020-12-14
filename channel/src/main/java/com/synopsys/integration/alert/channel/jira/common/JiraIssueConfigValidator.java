/**
 * channel
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
package com.synopsys.integration.alert.channel.jira.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.channel.jira.common.model.JiraIssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
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

    public JiraIssueConfig createValidIssueConfig(IssueTrackerContext context) throws AlertFieldException {
        JiraIssueConfig originalIssueConfig = (JiraIssueConfig) context.getIssueConfig();

        List<AlertFieldStatus> fieldErrors = new ArrayList<>();
        ProjectComponent projectComponent = validateProject(originalIssueConfig, fieldErrors);
        String validatedIssueCreator = validateIssueCreator(originalIssueConfig, fieldErrors);
        String validatedIssueType = validateIssueType(originalIssueConfig, fieldErrors);

        // FIXME validate custom fields
        //  this needs to be done when the UI design is finalized

        if (!fieldErrors.isEmpty()) {
            throw new AlertFieldException(JiraConstants.JIRA_ISSUE_VALIDATION_ERROR_MESSAGE, fieldErrors);
        }

        String projectName = null;
        String projectKey = null;
        String projectId = null;
        if (projectComponent != null) {
            projectName = projectComponent.getName();
            projectKey = projectComponent.getKey();
            projectId = projectComponent.getId();
        }

        return new JiraIssueConfig(
            projectName,
            projectKey,
            projectId,
            validatedIssueCreator,
            validatedIssueType,
            originalIssueConfig.getCommentOnIssues(),
            originalIssueConfig.getResolveTransition().orElse(null),
            originalIssueConfig.getOpenTransition().orElse(null),
            originalIssueConfig.getCustomFields()
        );
    }

    private ProjectComponent validateProject(JiraIssueConfig config, List<AlertFieldStatus> fieldErrors) {
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
                    fieldErrors.add(AlertFieldStatus.error(getProjectFieldKey(), String.format("No project named '%s' was found", jiraProjectName)));
                }
            } catch (IntegrationException e) {
                fieldErrors.add(AlertFieldStatus.error(getProjectFieldKey(), String.format(CONNECTION_ERROR_FORMAT_STRING, "projects")));
            }
        } else {
            requireField(fieldErrors, getProjectFieldKey());
        }
        return null;
    }

    private String validateIssueCreator(JiraIssueConfig config, List<AlertFieldStatus> fieldErrors) {
        String issueCreatorFieldKey = getIssueCreatorFieldKey();
        String issueCreator = config.getIssueCreator();
        if (StringUtils.isBlank(issueCreator)) {
            return null;
        }

        try {
            if (isUserValid(issueCreator)) {
                return issueCreator;
            } else {
                fieldErrors.add(AlertFieldStatus.error(issueCreatorFieldKey, String.format("The username '%s' is not associated with any valid Jira users.", issueCreator)));
            }
        } catch (IntegrationException e) {
            fieldErrors.add(AlertFieldStatus.error(issueCreatorFieldKey, String.format(CONNECTION_ERROR_FORMAT_STRING, "users")));
        }
        return null;
    }

    private String validateIssueType(JiraIssueConfig config, List<AlertFieldStatus> fieldErrors) {
        String issueType = config.getIssueType();
        if (StringUtils.isBlank(issueType)) {
            return null;
        }

        String issueTypeFieldKey = getIssueTypeFieldKey();
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
                        fieldErrors.add(AlertFieldStatus.error(issueTypeFieldKey, String.format("The issue type '%s' not assigned to project '%s'", issueType, projectName)));
                    }
                }
            } else {
                fieldErrors.add(AlertFieldStatus.error(issueTypeFieldKey, String.format("The issue type '%s' could not be found", issueType)));
            }
        } catch (IntegrationException e) {
            fieldErrors.add(AlertFieldStatus.error(issueTypeFieldKey, String.format(CONNECTION_ERROR_FORMAT_STRING, "issue types")));
        }
        return null;
    }

    private void requireField(List<AlertFieldStatus> fieldErrors, String key) {
        fieldErrors.add(AlertFieldStatus.error(key, "This field is required"));
    }

}

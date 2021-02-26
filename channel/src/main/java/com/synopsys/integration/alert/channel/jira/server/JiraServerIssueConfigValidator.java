/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server;

import java.util.Collection;

import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
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
        return JiraServerDescriptor.KEY_JIRA_PROJECT_NAME;
    }

    @Override
    public String getIssueTypeFieldKey() {
        return JiraServerDescriptor.KEY_ISSUE_TYPE;
    }

    @Override
    public String getIssueCreatorFieldKey() {
        return JiraServerDescriptor.KEY_ISSUE_CREATOR;
    }

    @Override
    public String getAddCommentsFieldKey() {
        return JiraServerDescriptor.KEY_ADD_COMMENTS;
    }

    @Override
    public String getResolveTransitionFieldKey() {
        return JiraServerDescriptor.KEY_RESOLVE_WORKFLOW_TRANSITION;
    }

    @Override
    public String getOpenTransitionFieldKey() {
        return JiraServerDescriptor.KEY_OPEN_WORKFLOW_TRANSITION;
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

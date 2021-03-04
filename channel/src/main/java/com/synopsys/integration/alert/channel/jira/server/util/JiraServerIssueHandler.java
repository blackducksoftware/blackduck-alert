/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.jira.common.JiraIssueSearchProperties;
import com.synopsys.integration.alert.channel.jira.common.util.JiraCallbackUtils;
import com.synopsys.integration.alert.channel.jira.common.util.JiraContentValidator;
import com.synopsys.integration.alert.channel.jira.common.util.JiraIssueHandler;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.descriptor.JiraServerDescriptor;
import com.synopsys.integration.alert.channel.jira2.common.JiraCustomFieldResolver;
import com.synopsys.integration.alert.channel.jira2.common.JiraErrorMessageUtility;
import com.synopsys.integration.alert.common.channel.issuetracker.config.IssueConfig;
import com.synopsys.integration.alert.common.channel.issuetracker.enumeration.IssueOperation;
import com.synopsys.integration.alert.common.channel.issuetracker.message.AlertIssueOrigin;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerCallbackInfo;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.common.channel.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ComponentItemCallbackInfo;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.request.IssueCommentRequestModel;
import com.synopsys.integration.jira.common.model.request.builder.IssueRequestModelFieldsMapBuilder;
import com.synopsys.integration.jira.common.model.response.IssueCreationResponseModel;
import com.synopsys.integration.jira.common.model.response.IssueResponseModel;
import com.synopsys.integration.jira.common.server.model.IssueCreationRequestModel;
import com.synopsys.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.synopsys.integration.jira.common.server.model.IssueSearchResponseModel;
import com.synopsys.integration.jira.common.server.service.IssueService;

public class JiraServerIssueHandler extends JiraIssueHandler {
    private static final Logger logger = LoggerFactory.getLogger(JiraServerIssueHandler.class);

    private final IssueService issueService;
    private final JiraServerProperties jiraProperties;

    private final JiraServerIssuePropertyHandler jiraIssuePropertyHelper;

    public JiraServerIssueHandler(IssueService issueService, JiraServerProperties jiraProperties, JiraErrorMessageUtility jiraErrorMessageUtility, JiraServerTransitionHandler jiraTransitionHandler,
        JiraServerIssuePropertyHandler jiraIssuePropertyHandler, JiraContentValidator jiraContentValidator, JiraCustomFieldResolver jiraCustomFieldResolver) {
        super(jiraErrorMessageUtility, jiraCustomFieldResolver, jiraTransitionHandler, jiraIssuePropertyHandler, jiraContentValidator);
        this.issueService = issueService;
        this.jiraProperties = jiraProperties;
        this.jiraIssuePropertyHelper = jiraIssuePropertyHandler;
    }

    @Override
    public IssueResponseModel createIssue(String issueCreator, String issueType, String projectName, IssueRequestModelFieldsMapBuilder fieldsBuilder) throws IntegrationException {
        IssueCreationResponseModel issueCreationResponseModel = issueService.createIssue(new IssueCreationRequestModel(issueCreator, issueType, projectName, fieldsBuilder));
        return issueService.getIssue(issueCreationResponseModel.getKey());
    }

    @Override
    public String getIssueCreatorFieldKey() {
        return JiraServerDescriptor.KEY_ISSUE_CREATOR;
    }

    @Override
    protected List<IssueResponseModel> retrieveExistingIssues(IssueConfig issueConfig, IssueTrackerRequest request)
        throws IntegrationException {
        JiraIssueSearchProperties issueProperties = request.getIssueSearchProperties();
        List<IssueSearchIssueComponent> searchIssueModels = jiraIssuePropertyHelper
                                                                .findIssues(issueConfig.getProjectKey(), issueProperties)
                                                                .map(IssueSearchResponseModel::getIssues)
                                                                .orElse(Collections.emptyList());
        List<IssueResponseModel> issues = new LinkedList<>();

        for (IssueSearchIssueComponent searchIssue : searchIssueModels) {
            try {
                issues.add(issueService.getIssue(searchIssue.getKey()));
            } catch (IntegrationException ex) {
                logger.error("Error getting issue details for issue. {}", searchIssue.getKey());
                logger.debug("Cause", ex);
            }
        }

        return issues;
    }

    @Override
    protected void addComment(IssueConfig issueConfig, String issueKey, String comment) throws IntegrationException {
        IssueCommentRequestModel issueCommentRequestModel = new IssueCommentRequestModel(issueKey, comment);
        issueService.addComment(issueCommentRequestModel);
    }

    @Override
    protected String getIssueKey(IssueResponseModel issueModel) {
        return issueModel.getKey();
    }

    @Override
    protected IssueTrackerIssueResponseModel createResponseModel(AlertIssueOrigin alertIssueOrigin, String issueTitle, IssueOperation issueOperation, IssueResponseModel issueResponse) {
        String uiLink = JiraCallbackUtils.createUILink(issueResponse);
        return new IssueTrackerIssueResponseModel(
            issueResponse.getKey(),
            uiLink,
            issueTitle,
            issueOperation,
            new IssueTrackerCallbackInfo(
                alertIssueOrigin.getProviderContentKey().getProviderConfigId(),
                alertIssueOrigin.getComponentItem().flatMap(ComponentItem::getCallbackInfo).map(ComponentItemCallbackInfo::getCallbackUrl).orElse(null),
                alertIssueOrigin.getProviderContentKey().getSubTopicValue() // FIXME this is wrong, but this class will be deleted before 6.5.0 is released
            )
        );
    }

    @Override
    protected String getIssueTrackerUrl() {
        return jiraProperties.getUrl();
    }

}

/*
 * channel-jira-server
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSenderFactory;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueCommenter;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueCreator;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueTransitioner;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.server.service.FieldService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;

@Component
public class JiraServerMessageSenderFactory implements IssueTrackerMessageSenderFactory<JiraServerJobDetailsModel, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraServerChannelKey channelKey;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;
    private final JiraErrorMessageUtility jiraErrorMessageUtility;

    @Autowired
    public JiraServerMessageSenderFactory(
        Gson gson,
        JiraServerChannelKey channelKey,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        JiraErrorMessageUtility jiraErrorMessageUtility
    ) {
        this.gson = gson;
        this.channelKey = channelKey;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.callbackInfoCreator = callbackInfoCreator;
        this.jiraErrorMessageUtility = jiraErrorMessageUtility;
    }

    @Override
    public IssueTrackerMessageSender<String> createMessageSender(JiraServerJobDetailsModel distributionDetails) throws AlertException {
        JiraServerProperties jiraServerProperties = jiraServerPropertiesFactory.createJiraProperties();
        JiraServerServiceFactory jiraServerServiceFactory = jiraServerProperties.createJiraServicesServerFactory(logger, gson);

        // Jira Services
        IssueService issueService = jiraServerServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraServerServiceFactory.createIssuePropertyService();

        // Common Helpers
        JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);

        return createMessageSender(issueService, distributionDetails, jiraServerServiceFactory, issuePropertiesManager);
    }

    public IssueTrackerMessageSender<String> createMessageSender(
        IssueService issueService,
        JiraServerJobDetailsModel distributionDetails,
        JiraServerServiceFactory jiraServerServiceFactory,
        JiraIssueAlertPropertiesManager issuePropertiesManager
    ) {
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);

        // Message Sender Requirements
        JiraServerIssueCommenter commenter = new JiraServerIssueCommenter(issueResponseCreator, issueService, distributionDetails);
        JiraServerIssueTransitioner transitioner = new JiraServerIssueTransitioner(commenter, issueResponseCreator, distributionDetails, issueService);
        JiraServerIssueCreator creator = createIssueCreator(distributionDetails, jiraServerServiceFactory, issuePropertiesManager, issueService, commenter);

        return new IssueTrackerMessageSender<>(creator, transitioner, commenter);
    }

    private JiraServerIssueCreator createIssueCreator(
        JiraServerJobDetailsModel distributionDetails,
        JiraServerServiceFactory jiraServerServiceFactory,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        IssueService issueService,
        JiraServerIssueCommenter issueCommenter
    ) {
        ProjectService projectService = jiraServerServiceFactory.createProjectService();
        FieldService fieldService = jiraServerServiceFactory.createFieldService();

        JiraCustomFieldResolver customFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
        JiraIssueCreationRequestCreator issueCreationRequestCreator = new JiraIssueCreationRequestCreator(customFieldResolver);

        return new JiraServerIssueCreator(
            channelKey,
            issueCommenter,
            callbackInfoCreator,
            distributionDetails,
            issueService,
            projectService,
            issueCreationRequestCreator,
            issuePropertiesManager,
            jiraErrorMessageUtility
        );
    }

}

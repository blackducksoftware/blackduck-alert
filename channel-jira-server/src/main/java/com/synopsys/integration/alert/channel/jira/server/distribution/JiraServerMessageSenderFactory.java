/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCommentEventGenerator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerCreationEventGenerator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerIssueResponseCreator;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSenderFactory;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerTransitionEventGenerator;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerCommentGenerator;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerCreateEventGenerator;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueCommenter;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueCreator;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueTransitioner;
import com.synopsys.integration.alert.channel.jira.server.distribution.delegate.JiraServerTransitionGenerator;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.server.service.FieldService;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
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
    private final IssueCategoryRetriever issueCategoryRetriever;

    private final EventManager eventManager;

    @Autowired
    public JiraServerMessageSenderFactory(
        Gson gson,
        JiraServerChannelKey channelKey,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        IssueCategoryRetriever issueCategoryRetriever,
        EventManager eventManager
    ) {
        this.gson = gson;
        this.channelKey = channelKey;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.callbackInfoCreator = callbackInfoCreator;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.eventManager = eventManager;
    }

    @Override
    public IssueTrackerMessageSender<String> createMessageSender(JiraServerJobDetailsModel distributionDetails, UUID globalId) throws AlertException {
        JiraServerProperties jiraServerProperties = jiraServerPropertiesFactory.createJiraProperties(globalId);
        JiraServerServiceFactory jiraServerServiceFactory = jiraServerProperties.createJiraServicesServerFactory(logger, gson);

        // Jira Services
        IssueService issueService = jiraServerServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraServerServiceFactory.createIssuePropertyService();
        IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();

        // Common Helpers
        JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);

        ProjectService projectService = jiraServerServiceFactory.createProjectService();
        FieldService fieldService = jiraServerServiceFactory.createFieldService();

        JiraServerQueryExecutor jiraServerQueryExecutor = new JiraServerQueryExecutor(issueSearchService);
        JiraCustomFieldResolver customFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
        JiraIssueCreationRequestCreator issueCreationRequestCreator = new JiraIssueCreationRequestCreator(customFieldResolver);
        JiraErrorMessageUtility jiraErrorMessageUtility = new JiraErrorMessageUtility(gson, customFieldResolver);

        return createMessageSender(
            issueService,
            distributionDetails,
            projectService,
            issueCreationRequestCreator,
            issuePropertiesManager,
            jiraErrorMessageUtility,
            jiraServerQueryExecutor
        );
    }

    public IssueTrackerMessageSender<String> createMessageSender(
        IssueService issueService,
        JiraServerJobDetailsModel distributionDetails,
        ProjectService projectService,
        JiraIssueCreationRequestCreator issueCreationRequestCreator,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        JiraServerQueryExecutor jiraServerQueryExecutor
    ) {
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);

        // Message Sender Requirements
        JiraServerIssueCommenter commenter = new JiraServerIssueCommenter(issueResponseCreator, issueService, distributionDetails);
        JiraServerIssueTransitioner transitioner = new JiraServerIssueTransitioner(commenter, issueResponseCreator, distributionDetails, issueService);
        JiraServerIssueCreator creator = new JiraServerIssueCreator(
            channelKey,
            commenter,
            callbackInfoCreator,
            distributionDetails,
            issueService,
            projectService,
            issueCreationRequestCreator,
            issuePropertiesManager,
            jiraErrorMessageUtility,
            issueCategoryRetriever,
            jiraServerQueryExecutor
        );
        UUID jobId = distributionDetails.getJobId();
        IssueTrackerCommentEventGenerator<String> commentEventGenerator = new JiraServerCommentGenerator(channelKey, jobId);
        IssueTrackerCreationEventGenerator createEventGenerator = new JiraServerCreateEventGenerator(channelKey, jobId);
        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = new JiraServerTransitionGenerator(channelKey, jobId);

        return new IssueTrackerMessageSender<>(creator, transitioner, commenter, createEventGenerator, transitionEventGenerator, commentEventGenerator, eventManager);

    }

}

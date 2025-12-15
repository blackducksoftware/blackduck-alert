/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.callback.IssueTrackerCallbackInfoCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerModelHolder;
import com.blackduck.integration.alert.api.channel.issue.tracker.search.IssueCategoryRetriever;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.DefaultIssueTrackerEventGenerator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerAsyncMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCommentEventGenerator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerCreationEventGenerator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerIssueResponseCreator;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSender;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSenderFactory;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerTransitionEventGenerator;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.distribution.delegate.JiraServerCommentGenerator;
import com.blackduck.integration.alert.channel.jira.server.distribution.delegate.JiraServerCreateEventGenerator;
import com.blackduck.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueCommenter;
import com.blackduck.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueCreator;
import com.blackduck.integration.alert.channel.jira.server.distribution.delegate.JiraServerIssueTransitioner;
import com.blackduck.integration.alert.channel.jira.server.distribution.delegate.JiraServerTransitionGenerator;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.blackduck.integration.jira.common.server.builder.IssueRequestModelFieldsBuilder;
import com.blackduck.integration.jira.common.server.service.FieldService;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.IssueService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.blackduck.integration.jira.common.server.service.ProjectService;
import com.google.gson.Gson;

@Component
public class JiraServerMessageSenderFactory implements IssueTrackerMessageSenderFactory<JiraServerJobDetailsModel, String, IssueTrackerModelHolder<String>> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraServerChannelKey channelKey;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;
    private final IssueCategoryRetriever issueCategoryRetriever;

    private final EventManager eventManager;

    private final ExecutingJobManager executingJobManager;

    @Autowired
    public JiraServerMessageSenderFactory(
        Gson gson,
        JiraServerChannelKey channelKey,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        IssueCategoryRetriever issueCategoryRetriever,
        EventManager eventManager,
        ExecutingJobManager executingJobManager
    ) {
        this.gson = gson;
        this.channelKey = channelKey;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.callbackInfoCreator = callbackInfoCreator;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.eventManager = eventManager;
        this.executingJobManager = executingJobManager;
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
        JiraIssueCreationRequestCreator issueCreationRequestCreator = new JiraIssueCreationRequestCreator(customFieldResolver, IssueRequestModelFieldsBuilder::new);
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

    @Override
    public IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<String>> createAsyncMessageSender(
        JiraServerJobDetailsModel distributionDetails,
        UUID globalId,
        UUID jobExecutionId,
        Set<Long> notificationIds
    ) throws AlertException {
        return createAsyncMessageSender(distributionDetails, jobExecutionId, notificationIds);
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

        return new IssueTrackerMessageSender<>(
            creator,
            transitioner,
            commenter
        );

    }

    public IssueTrackerAsyncMessageSender<IssueTrackerModelHolder<String>> createAsyncMessageSender(
        JiraServerJobDetailsModel distributionDetails,
        UUID jobExecutionId,
        Set<Long> notificationIds
    ) {
        UUID jobId = distributionDetails.getJobId();
        IssueTrackerCommentEventGenerator<String> commentEventGenerator = new JiraServerCommentGenerator(channelKey, jobExecutionId, jobId, notificationIds);
        IssueTrackerCreationEventGenerator createEventGenerator = new JiraServerCreateEventGenerator(channelKey, jobExecutionId, jobId, notificationIds);
        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = new JiraServerTransitionGenerator(
            channelKey,
            jobExecutionId,
            jobId,
            notificationIds
        );

        DefaultIssueTrackerEventGenerator<String> eventGenerator = new DefaultIssueTrackerEventGenerator<>(createEventGenerator, transitionEventGenerator, commentEventGenerator);

        return new IssueTrackerAsyncMessageSender<>(
            eventGenerator,
            eventManager,
            jobExecutionId,
            notificationIds,
            executingJobManager
        );

    }

}

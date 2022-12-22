/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution;

import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.callback.IssueTrackerCallbackInfoCreator;
import com.synopsys.integration.alert.api.channel.issue.search.IssueCategoryRetriever;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerAsyncMessageSender;
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
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.delegate.JiraCloudCommentGenerator;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.delegate.JiraCloudCreateEventGenerator;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.delegate.JiraCloudIssueCommenter;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.delegate.JiraCloudIssueCreator;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.delegate.JiraCloudIssueTransitioner;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.delegate.JiraCloudTransitionGenerator;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;
import com.synopsys.integration.jira.common.cloud.service.FieldService;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;

@Component
public class JiraCloudMessageSenderFactory implements IssueTrackerMessageSenderFactory<JiraCloudJobDetailsModel, String> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraCloudChannelKey channelKey;
    private final JiraCloudPropertiesFactory jiraCloudPropertiesFactory;
    private final IssueTrackerCallbackInfoCreator callbackInfoCreator;
    private final IssueCategoryRetriever issueCategoryRetriever;
    private final EventManager eventManager;
    private final JobSubTaskAccessor jobSubTaskAccessor;

    @Autowired
    public JiraCloudMessageSenderFactory(
        Gson gson,
        JiraCloudChannelKey channelKey,
        JiraCloudPropertiesFactory jiraCloudPropertiesFactory,
        IssueTrackerCallbackInfoCreator callbackInfoCreator,
        IssueCategoryRetriever issueCategoryRetriever,
        EventManager eventManager,
        JobSubTaskAccessor jobSubTaskAccessor
    ) {
        this.gson = gson;
        this.channelKey = channelKey;
        this.jiraCloudPropertiesFactory = jiraCloudPropertiesFactory;
        this.callbackInfoCreator = callbackInfoCreator;
        this.issueCategoryRetriever = issueCategoryRetriever;
        this.eventManager = eventManager;
        this.jobSubTaskAccessor = jobSubTaskAccessor;
    }

    @Override
    public IssueTrackerMessageSender<String> createMessageSender(JiraCloudJobDetailsModel distributionDetails, UUID globalId) throws AlertException {
        JiraCloudProperties jiraCloudProperties = jiraCloudPropertiesFactory.createJiraProperties();
        JiraCloudServiceFactory jiraCloudServiceFactory = jiraCloudProperties.createJiraServicesCloudFactory(logger, gson);

        // Jira Services
        IssueService issueService = jiraCloudServiceFactory.createIssueService();
        IssuePropertyService issuePropertyService = jiraCloudServiceFactory.createIssuePropertyService();
        IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();
        // Common Helpers
        JiraCloudQueryExecutor jiraCloudQueryExecutor = new JiraCloudQueryExecutor(issueSearchService);
        JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);

        ProjectService projectService = jiraCloudServiceFactory.createProjectService();
        FieldService fieldService = jiraCloudServiceFactory.createFieldService();

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
            jiraCloudQueryExecutor
        );
    }

    @Override
    public IssueTrackerAsyncMessageSender<String> createAsyncMessageSender(
        JiraCloudJobDetailsModel distributionDetails,
        UUID globalId,
        UUID parentEventId,
        Set<Long> notificationIds
    ) throws AlertException {
        return createAsyncMessageSender(distributionDetails, parentEventId, notificationIds);
    }

    public IssueTrackerMessageSender<String> createMessageSender(
        IssueService issueService,
        JiraCloudJobDetailsModel distributionDetails,
        ProjectService projectService,
        JiraIssueCreationRequestCreator issueCreationRequestCreator,
        JiraIssueAlertPropertiesManager issuePropertiesManager,
        JiraErrorMessageUtility jiraErrorMessageUtility,
        JiraCloudQueryExecutor jiraCloudQueryExecutor
    ) {
        // Jira Services
        IssueTrackerIssueResponseCreator issueResponseCreator = new IssueTrackerIssueResponseCreator(callbackInfoCreator);

        // Message Sender Requirements
        JiraCloudIssueCommenter issueCommenter = new JiraCloudIssueCommenter(issueResponseCreator, issueService, distributionDetails);
        JiraCloudIssueTransitioner issueTransitioner = new JiraCloudIssueTransitioner(issueCommenter, issueResponseCreator, distributionDetails, issueService);
        JiraCloudIssueCreator issueCreator = new JiraCloudIssueCreator(
            channelKey,
            issueCommenter,
            callbackInfoCreator,
            distributionDetails,
            issueService,
            projectService,
            issueCreationRequestCreator,
            issuePropertiesManager,
            jiraErrorMessageUtility,
            issueCategoryRetriever,
            jiraCloudQueryExecutor
        );

        return new IssueTrackerMessageSender<>(
            issueCreator,
            issueTransitioner,
            issueCommenter
        );
    }

    public IssueTrackerAsyncMessageSender<String> createAsyncMessageSender(
        JiraCloudJobDetailsModel distributionDetails,
        UUID jobExecutionId,
        Set<Long> notificationIds
    ) {
        UUID jobId = distributionDetails.getJobId();
        IssueTrackerCommentEventGenerator<String> commentEventGenerator = new JiraCloudCommentGenerator(channelKey, jobExecutionId, jobId, notificationIds);
        IssueTrackerCreationEventGenerator createEventGenerator = new JiraCloudCreateEventGenerator(channelKey, jobExecutionId, jobId, notificationIds);
        IssueTrackerTransitionEventGenerator<String> transitionEventGenerator = new JiraCloudTransitionGenerator(channelKey, jobExecutionId, jobId, notificationIds);

        return new IssueTrackerAsyncMessageSender<>(
            createEventGenerator,
            transitionEventGenerator,
            commentEventGenerator,
            eventManager,
            jobSubTaskAccessor,
            jobExecutionId,
            notificationIds
        );

    }

}

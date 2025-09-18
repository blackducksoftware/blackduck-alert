/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.distribution.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.blackduck.integration.jira.common.server.builder.IssueRequestModelFieldsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCommentEventHandler;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCommentModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerIssueResponseModel;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueTrackerResponse;
import com.blackduck.integration.alert.api.channel.issue.tracker.send.IssueTrackerMessageSender;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.blackduck.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.blackduck.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.blackduck.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.blackduck.integration.alert.channel.jira.server.distribution.JiraServerQueryExecutor;
import com.blackduck.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.blackduck.integration.jira.common.server.service.FieldService;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.IssueService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.blackduck.integration.jira.common.server.service.ProjectService;
import com.google.gson.Gson;

@Component
public class JiraServerCommentEventHandler extends IssueTrackerCommentEventHandler<JiraServerCommentEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final JiraServerMessageSenderFactory jiraServerMessageSenderFactory;
    private final JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public JiraServerCommentEventHandler(
        EventManager eventManager,
        Gson gson,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory,
        JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor,
        IssueTrackerResponsePostProcessor responsePostProcessor,
        ExecutingJobManager executingJobManager
    ) {
        super(eventManager, responsePostProcessor, executingJobManager);
        this.gson = gson;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.jiraServerMessageSenderFactory = jiraServerMessageSenderFactory;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public void handleEvent(JiraServerCommentEvent event) {
        UUID jobId = event.getJobId();
        Optional<JiraServerJobDetailsModel> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraPropertiesWithJobId(jobId);
                JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);

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

                IssueTrackerMessageSender<String> messageSender = jiraServerMessageSenderFactory.createMessageSender(
                    issueService,
                    details.get(),
                    projectService,
                    issueCreationRequestCreator,
                    issuePropertiesManager,
                    jiraErrorMessageUtility,
                    jiraServerQueryExecutor
                );
                IssueCommentModel<String> commentModel = event.getCommentModel();
                List<IssueTrackerIssueResponseModel<String>> responses = messageSender.sendMessage(commentModel);
                postProcess(new IssueTrackerResponse<>("Success", responses));
            } catch (AlertException ex) {
                logger.error("Cannot comment on issue for job {}", jobId);
                logger.error("Cause: ", ex);
            }
        } else {
            logger.error("No Jira Server job found with id {}", jobId);
        }
    }
}

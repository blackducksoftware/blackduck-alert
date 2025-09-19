/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.cloud.distribution.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.blackduck.integration.jira.common.cloud.builder.IssueRequestModelFieldsBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.issue.tracker.IssueTrackerResponsePostProcessor;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEvent;
import com.blackduck.integration.alert.api.channel.issue.tracker.event.IssueTrackerCreateIssueEventHandler;
import com.blackduck.integration.alert.api.channel.issue.tracker.model.IssueCreationModel;
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
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.cloud.distribution.JiraCloudMessageSenderFactory;
import com.blackduck.integration.alert.channel.jira.cloud.distribution.JiraCloudQueryExecutor;
import com.blackduck.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.blackduck.integration.jira.common.cloud.service.FieldService;
import com.blackduck.integration.jira.common.cloud.service.IssueSearchService;
import com.blackduck.integration.jira.common.cloud.service.IssueService;
import com.blackduck.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.blackduck.integration.jira.common.cloud.service.ProjectService;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.google.gson.Gson;

@Component
public class JiraCloudCreateIssueEventHandler extends IssueTrackerCreateIssueEventHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final JiraCloudPropertiesFactory jiraCloudPropertiesFactory;
    private final JiraCloudMessageSenderFactory messageSenderFactory;

    private final JobDetailsAccessor<JiraCloudJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public JiraCloudCreateIssueEventHandler(
        EventManager eventManager,
        Gson gson,
        JiraCloudPropertiesFactory jiraCloudPropertiesFactory,
        JiraCloudMessageSenderFactory messageSenderFactory,
        JobDetailsAccessor<JiraCloudJobDetailsModel> jobDetailsAccessor,
        IssueTrackerResponsePostProcessor responsePostProcessor,
        ExecutingJobManager executingJobManager
    ) {
        super(eventManager, responsePostProcessor, executingJobManager);
        this.gson = gson;
        this.jiraCloudPropertiesFactory = jiraCloudPropertiesFactory;
        this.messageSenderFactory = messageSenderFactory;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public synchronized void handleEvent(IssueTrackerCreateIssueEvent event) {
        UUID jobId = event.getJobId();
        IssueCreationModel creationModel = event.getCreationModel();
        Optional<JiraCloudJobDetailsModel> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                JiraCloudProperties jiraProperties = jiraCloudPropertiesFactory.createJiraProperties();
                JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);

                // Jira Services
                IssueService issueService = jiraCloudServiceFactory.createIssueService();
                IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();
                IssuePropertyService issuePropertyService = jiraCloudServiceFactory.createIssuePropertyService();

                // Common Helpers
                JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);
                JiraCloudQueryExecutor jiraCloudQueryExecutor = new JiraCloudQueryExecutor(issueSearchService);

                ProjectService projectService = jiraCloudServiceFactory.createProjectService();
                FieldService fieldService = jiraCloudServiceFactory.createFieldService();

                JiraCustomFieldResolver customFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
                JiraIssueCreationRequestCreator issueCreationRequestCreator = new JiraIssueCreationRequestCreator(customFieldResolver, IssueRequestModelFieldsBuilder::new);
                JiraErrorMessageUtility jiraErrorMessageUtility = new JiraErrorMessageUtility(gson, customFieldResolver);

                IssueTrackerMessageSender<String> messageSender = messageSenderFactory.createMessageSender(
                    issueService,
                    details.get(),
                    projectService,
                    issueCreationRequestCreator,
                    issuePropertiesManager,
                    jiraErrorMessageUtility,
                    jiraCloudQueryExecutor
                );

                String jqlQuery = creationModel.getQueryString().orElse(null);
                boolean issueDoesNotExist = checkIfIssueDoesNotExist(jiraCloudQueryExecutor, jqlQuery);
                if (issueDoesNotExist) {
                    List<IssueTrackerIssueResponseModel<String>> responses = messageSender.sendMessage(creationModel);
                    postProcess(new IssueTrackerResponse<>("Success", responses));
                    List<String> issueKeys = responses.stream()
                        .map(IssueTrackerIssueResponseModel::getIssueId)
                        .collect(Collectors.toList());
                    logger.info("Created issues: {}", issueKeys);
                }
            } catch (AlertException ex) {
                logger.error("Cannot create issue for job {}", jobId);
                logger.error("Query: {}", creationModel.getQueryString());
                logger.error("Cause: ", ex);
            }
        } else {
            logger.error("No Jira Cloud job found with id {}", jobId);
        }
    }

    private boolean checkIfIssueDoesNotExist(JiraCloudQueryExecutor executor, String query) {
        if (StringUtils.isBlank(query)) {
            return true;
        }

        try {
            return executor.executeQuery(query).isEmpty();
        } catch (AlertException ex) {
            logger.error("Query executed: {}", query);
            logger.error("Couldn't execute query to see if issue exists.", ex);
        }
        return true;
    }

}

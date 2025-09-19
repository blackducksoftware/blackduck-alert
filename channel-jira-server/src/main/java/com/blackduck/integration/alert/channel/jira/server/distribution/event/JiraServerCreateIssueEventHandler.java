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
import java.util.stream.Collectors;

import com.blackduck.integration.jira.common.server.builder.IssueRequestModelFieldsBuilder;
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
public class JiraServerCreateIssueEventHandler extends IssueTrackerCreateIssueEventHandler {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final JiraServerMessageSenderFactory jiraServerMessageSenderFactory;
    private final JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public JiraServerCreateIssueEventHandler(
        EventManager eventManager,
        Gson gson,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory,
        JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor,
        IssueTrackerResponsePostProcessor issueTrackerResponsePostProcessor,
        ExecutingJobManager executingJobManager
    ) {
        super(eventManager, issueTrackerResponsePostProcessor, executingJobManager);
        this.gson = gson;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.jiraServerMessageSenderFactory = jiraServerMessageSenderFactory;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public synchronized void handleEvent(IssueTrackerCreateIssueEvent event) {
        UUID jobId = event.getJobId();
        IssueCreationModel creationModel = event.getCreationModel();
        Optional<JiraServerJobDetailsModel> details = jobDetailsAccessor.retrieveDetails(jobId);
        if (details.isPresent()) {
            try {
                JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraPropertiesWithJobId(jobId);
                JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);

                // Jira Services
                IssueService issueService = jiraServerServiceFactory.createIssueService();
                IssueSearchService issueSearchService = jiraServerServiceFactory.createIssueSearchService();
                IssuePropertyService issuePropertyService = jiraServerServiceFactory.createIssuePropertyService();

                // Common Helpers
                JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);
                JiraServerQueryExecutor jiraServerQueryExecutor = new JiraServerQueryExecutor(issueSearchService);

                ProjectService projectService = jiraServerServiceFactory.createProjectService();
                FieldService fieldService = jiraServerServiceFactory.createFieldService();

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

                String jqlQuery = creationModel.getQueryString().orElse(null);
                boolean issueDoesNotExist = checkIfIssueDoesNotExist(jiraServerQueryExecutor, jqlQuery);
                if (issueDoesNotExist) {
                    List<IssueTrackerIssueResponseModel<String>> responses = messageSender.sendMessage(creationModel);
                    postProcess(new IssueTrackerResponse<>("Success", responses));
                    List<String> issueKeys = responses.stream()
                        .map(IssueTrackerIssueResponseModel::getIssueId)
                        .collect(Collectors.toList());
                    logger.info("Created issues: {}", issueKeys);
                } else {
                    logger.debug("Issue already exists for query: {}", jqlQuery);
                }
            } catch (AlertException ex) {
                logger.error("Cannot create issue for job {}", jobId);
                logger.error("Query: {}", creationModel.getQueryString());
                logger.error("Cause: ", ex);
            }
        } else {
            logger.error("No Jira Server job found with id {}", jobId);
        }
    }

    private boolean checkIfIssueDoesNotExist(JiraServerQueryExecutor executor, String query) {
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

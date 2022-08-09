/*
 * channel-jira-cloud
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.cloud.distribution.event;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerCommentEventHandler;
import com.synopsys.integration.alert.api.channel.issue.model.IssueCommentModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.synopsys.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.JiraCloudMessageSenderFactory;
import com.synopsys.integration.alert.channel.jira.cloud.distribution.JiraCloudQueryExecutor;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraCloudJobDetailsModel;
import com.synopsys.integration.jira.common.cloud.service.FieldService;
import com.synopsys.integration.jira.common.cloud.service.IssueSearchService;
import com.synopsys.integration.jira.common.cloud.service.IssueService;
import com.synopsys.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.synopsys.integration.jira.common.cloud.service.ProjectService;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;

@Component
public class JiraCloudCommentEventHandler implements IssueTrackerCommentEventHandler<JiraCloudCommentEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final Gson gson;
    private final JiraCloudPropertiesFactory jiraCloudPropertiesFactory;
    private final JiraCloudMessageSenderFactory messageSenderFactory;

    private final JobDetailsAccessor<JiraCloudJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public JiraCloudCommentEventHandler(
        Gson gson,
        JiraCloudPropertiesFactory jiraCloudPropertiesFactory,
        JiraCloudMessageSenderFactory messageSenderFactory,
        JobDetailsAccessor<JiraCloudJobDetailsModel> jobDetailsAccessor
    ) {
        this.gson = gson;
        this.jiraCloudPropertiesFactory = jiraCloudPropertiesFactory;
        this.messageSenderFactory = messageSenderFactory;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public void handle(JiraCloudCommentEvent event) {
        UUID jobId = event.getJobId();
        Optional<JiraCloudJobDetailsModel> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                JiraCloudProperties jiraProperties = jiraCloudPropertiesFactory.createJiraProperties();
                JiraCloudServiceFactory jiraCloudServiceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);

                // Jira Services
                IssueService issueService = jiraCloudServiceFactory.createIssueService();
                IssuePropertyService issuePropertyService = jiraCloudServiceFactory.createIssuePropertyService();
                IssueSearchService issueSearchService = jiraCloudServiceFactory.createIssueSearchService();

                // Common Helpers
                JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);
                JiraCloudQueryExecutor jiraCloudQueryExecutor = new JiraCloudQueryExecutor(issueSearchService);

                ProjectService projectService = jiraCloudServiceFactory.createProjectService();
                FieldService fieldService = jiraCloudServiceFactory.createFieldService();

                JiraCustomFieldResolver customFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
                JiraIssueCreationRequestCreator issueCreationRequestCreator = new JiraIssueCreationRequestCreator(customFieldResolver);
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
                IssueCommentModel<String> commentModel = event.getCommentModel();
                messageSender.sendMessage(commentModel);
            } catch (AlertException ex) {
                logger.error("Cannot comment on issue for job {}", jobId);
            }
        } else {
            logger.error("No Jira Cloud job found with id {}", jobId);
        }
    }

}

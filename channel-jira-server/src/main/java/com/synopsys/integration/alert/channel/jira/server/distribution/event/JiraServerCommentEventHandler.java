/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution.event;

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
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.server.service.FieldService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;

@Component
public class JiraServerCommentEventHandler implements IssueTrackerCommentEventHandler<JiraServerCommentEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final JiraServerMessageSenderFactory jiraServerMessageSenderFactory;
    private final JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public JiraServerCommentEventHandler(
        Gson gson,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory,
        JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor
    ) {
        this.gson = gson;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.jiraServerMessageSenderFactory = jiraServerMessageSenderFactory;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public void handle(JiraServerCommentEvent event) {
        logger.info("Jira Server comment handler");
        UUID jobId = event.getJobId();
        Optional<JiraServerJobDetailsModel> details = jobDetailsAccessor.retrieveDetails(event.getJobId());
        if (details.isPresent()) {
            try {
                JiraServerProperties jiraProperties = jiraServerPropertiesFactory.createJiraPropertiesWithJobId(jobId);
                JiraServerServiceFactory jiraServerServiceFactory = jiraProperties.createJiraServicesServerFactory(logger, gson);

                // Jira Services
                IssueService issueService = jiraServerServiceFactory.createIssueService();
                IssuePropertyService issuePropertyService = jiraServerServiceFactory.createIssuePropertyService();

                // Common Helpers
                JiraIssueAlertPropertiesManager issuePropertiesManager = new JiraIssueAlertPropertiesManager(gson, issuePropertyService);

                ProjectService projectService = jiraServerServiceFactory.createProjectService();
                FieldService fieldService = jiraServerServiceFactory.createFieldService();

                JiraCustomFieldResolver customFieldResolver = new JiraCustomFieldResolver(fieldService::getUserVisibleFields);
                JiraIssueCreationRequestCreator issueCreationRequestCreator = new JiraIssueCreationRequestCreator(customFieldResolver);
                JiraErrorMessageUtility jiraErrorMessageUtility = new JiraErrorMessageUtility(gson, customFieldResolver);

                IssueTrackerMessageSender<String> messageSender = jiraServerMessageSenderFactory.createMessageSender(
                    issueService,
                    details.get(),
                    projectService,
                    issueCreationRequestCreator,
                    issuePropertiesManager,
                    jiraErrorMessageUtility
                );
                IssueCommentModel<String> commentModel = event.getCommentModel();
                messageSender.sendMessage(commentModel);
            } catch (AlertException ex) {
                logger.error("Cannot comment on issue for job {}", jobId);
                logger.error("Cause: ", ex);
            }
        } else {
            logger.error("No Jira Server job found with id {}", jobId);
        }
    }
}

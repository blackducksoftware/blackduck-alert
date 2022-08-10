/*
 * channel-jira-server
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira.server.distribution.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.issue.IssueTrackerResponsePostProcessor;
import com.synopsys.integration.alert.api.channel.issue.event.IssueTrackerTransitionEventHandler;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerIssueResponseModel;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTrackerResponse;
import com.synopsys.integration.alert.api.channel.issue.model.IssueTransitionModel;
import com.synopsys.integration.alert.api.channel.issue.send.IssueTrackerMessageSender;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraErrorMessageUtility;
import com.synopsys.integration.alert.api.channel.jira.distribution.JiraIssueCreationRequestCreator;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.synopsys.integration.alert.api.channel.jira.distribution.search.JiraIssueAlertPropertiesManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.channel.jira.server.JiraServerProperties;
import com.synopsys.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerMessageSenderFactory;
import com.synopsys.integration.alert.channel.jira.server.distribution.JiraServerQueryExecutor;
import com.synopsys.integration.alert.common.persistence.accessor.JobDetailsAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobSubTaskAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;
import com.synopsys.integration.jira.common.rest.service.IssuePropertyService;
import com.synopsys.integration.jira.common.server.service.FieldService;
import com.synopsys.integration.jira.common.server.service.IssueSearchService;
import com.synopsys.integration.jira.common.server.service.IssueService;
import com.synopsys.integration.jira.common.server.service.JiraServerServiceFactory;
import com.synopsys.integration.jira.common.server.service.ProjectService;

@Component
public class JiraServerTransitionEventHandler extends IssueTrackerTransitionEventHandler<JiraServerTransitionEvent> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson;
    private final JiraServerPropertiesFactory jiraServerPropertiesFactory;
    private final JiraServerMessageSenderFactory jiraServerMessageSenderFactory;
    private final JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor;

    @Autowired
    public JiraServerTransitionEventHandler(
        EventManager eventManager,
        JobSubTaskAccessor jobSubTaskAccessor,
        Gson gson,
        JiraServerPropertiesFactory jiraServerPropertiesFactory,
        JiraServerMessageSenderFactory jiraServerMessageSenderFactory,
        JobDetailsAccessor<JiraServerJobDetailsModel> jobDetailsAccessor,
        IssueTrackerResponsePostProcessor responsePostProcessor
    ) {
        super(eventManager, jobSubTaskAccessor, responsePostProcessor);
        this.gson = gson;
        this.jiraServerPropertiesFactory = jiraServerPropertiesFactory;
        this.jiraServerMessageSenderFactory = jiraServerMessageSenderFactory;
        this.jobDetailsAccessor = jobDetailsAccessor;
    }

    @Override
    public void handleEvent(JiraServerTransitionEvent event) {
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
                JiraServerQueryExecutor jiraServerQueryExecutor = new JiraServerQueryExecutor(issueSearchService);
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
                    jiraErrorMessageUtility,
                    jiraServerQueryExecutor
                );
                IssueTransitionModel<String> transitionModel = event.getTransitionModel();
                List<IssueTrackerIssueResponseModel<String>> responses = messageSender.sendMessage(transitionModel);
                postProcess(new IssueTrackerResponse<>("Success", responses));
            } catch (AlertException ex) {
                logger.error("Cannot transition issue for job {}", jobId);
            }
        } else {
            logger.error("No Jira Server job found with id {}", jobId);
        }
    }
}

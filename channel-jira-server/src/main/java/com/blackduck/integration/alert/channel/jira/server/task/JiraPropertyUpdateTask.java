/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.task;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.blackduck.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.blackduck.integration.jira.common.server.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JiraPropertyUpdateTask extends JiraTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JiraServerPropertiesFactory jiraPropertiesFactory;
    private final Set<String> projectNamesOrKeys;

    public JiraPropertyUpdateTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraServerPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix, Set<String> projectNamesOrKeys) {
        super(taskScheduler, taskManager, configId, configName, taskNameSuffix, gson);
        this.jiraPropertiesFactory = jiraPropertiesFactory;
        this.projectNamesOrKeys = projectNamesOrKeys;
    }

    @Override
    protected void executeTaskImplementation() {
        logger.info("Jira Server property migrator task started.");
        UUID configId = UUID.fromString(getConfigId());
        try {
            JiraServerProperties jiraProperties = jiraPropertiesFactory.createJiraProperties(configId);
            JiraServerServiceFactory serviceFactory = jiraProperties.createJiraServicesServerFactory(logger,getGson());
            IssueSearchService issueSearchService = serviceFactory.createIssueSearchService();
            IssuePropertyService issuePropertyService = serviceFactory.createIssuePropertyService();
            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, 0, JQL_QUERY_MAX_RESULTS);
            boolean foundIssuesForDefaultQuery = updateIssues(responseModel, issuePropertyService);

            if(!foundIssuesForDefaultQuery) {
                if (!projectNamesOrKeys.isEmpty()) {
                    String projectName = projectNamesOrKeys.stream().findFirst().orElse("");
                    if(StringUtils.isNotBlank(projectName)) {
                        logger.info("Querying issues for {} remaining project(s).  Querying issues for project: {}. ", projectNamesOrKeys.size(), projectName);
                        logger.debug("Remaining projects: {}", projectNamesOrKeys);
                        responseModel = issueSearchService.queryForIssuePage(createProjectSpecificQuery(projectName), 0, JQL_QUERY_MAX_RESULTS);
                        boolean foundIssuesForProject = updateIssues(responseModel, issuePropertyService);
                        if (!foundIssuesForProject) {
                            // remove the key to no longer query for that project.
                            projectNamesOrKeys.remove(projectName);
                        }
                    }
                } else {
                    // unschedule the task because the default query returned no results and there are no specific
                    // projects to query for.
                    unScheduleTask();
                }
            }
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Server Configuration.", e);
        } catch (InterruptedException e) {
            logger.error("Error updating Jira Server issues with new property.", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Jira Server property migrator task ended.");
    }

    private boolean updateIssues(IssueSearchResponseModel responseModel, IssuePropertyService issuePropertyService) throws InterruptedException {
        int totalIssues = responseModel.getTotal();
        boolean foundIssues = totalIssues > 0;

        if(foundIssues) {
            List<String> issueKeys = responseModel.getIssues().stream()
                    .map(IssueSearchIssueComponent::getKey)
                    .toList();
            updateIssues(issueKeys, totalIssues, issuePropertyService);
        }
        return foundIssues;
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_30_SECONDS_CRON_EXPRESSION;
    }
}

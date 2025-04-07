package com.blackduck.integration.alert.channel.jira.server.action;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.model.response.IssuePropertyResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.blackduck.integration.jira.common.server.model.IssueSearchIssueComponent;
import com.blackduck.integration.jira.common.server.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.server.service.IssueSearchService;
import com.blackduck.integration.jira.common.server.service.JiraServerServiceFactory;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.UUID;

public class JiraPropertyMigratorTask extends JiraTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JiraServerPropertiesFactory jiraPropertiesFactory;
    private final Gson gson;
    public JiraPropertyMigratorTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraServerPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix) {
        super(taskScheduler, taskManager, configId, configName, taskNameSuffix);
        this.jiraPropertiesFactory = jiraPropertiesFactory;
        this.gson = gson;
    }

    @Override
    protected void executeTaskImplementation() {
        logger.info("Jira Server property migrator task started.");
        UUID configId = UUID.fromString(getConfigId());
        try {
            JiraServerProperties jiraProperties = jiraPropertiesFactory.createJiraProperties(configId);
            JiraServerServiceFactory serviceFactory = jiraProperties.createJiraServicesServerFactory(logger,gson);
            IssueSearchService issueSearchService = serviceFactory.createIssueSearchService();
            IssuePropertyService issuePropertyService = serviceFactory.createIssuePropertyService();
            int startOffset = 0;
            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, startOffset, JQL_QUERY_MAX_RESULTS);

            while(startOffset <= responseModel.getTotal()) {
                List<IssueSearchIssueComponent> issueList = responseModel.getIssues();
                for (IssueSearchIssueComponent issue : issueList) {
                    try {
                        String issueKey = issue.getKey();
                        IssuePropertyResponseModel issuePropertyResponse = issuePropertyService.getProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_OLD_KEY);
                        issuePropertyService.setProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, gson.toJson(issuePropertyResponse.getValue()));
                    } catch (IntegrationException e) {
                        logger.error(String.format("Error migrating issue property for issue: %s",issue.getKey()), e);
                    }
                }
                startOffset += JQL_QUERY_MAX_RESULTS;
                responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, startOffset, JQL_QUERY_MAX_RESULTS);
            }

            responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, 0, 1);
            if(responseModel.getTotal() <= 0) {
                unScheduleTask();
            }
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Server Configuration.", e);
        }

        logger.info("Jira Server property migrator task ended.");
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION;
    }
}

package com.blackduck.integration.alert.channel.jira.cloud.action;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.blackduck.integration.alert.common.channel.issuetracker.exception.IssueTrackerException;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.cloud.service.IssueSearchService;
import com.blackduck.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.blackduck.integration.jira.common.model.response.IssuePropertyResponseModel;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;

public class JiraPropertyMigratorTask extends JiraTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JiraCloudPropertiesFactory jiraPropertiesFactory;
    private final Gson gson;

    public JiraPropertyMigratorTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraCloudPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix) {
        super(taskScheduler, taskManager, configId, configName, taskNameSuffix);
        this.jiraPropertiesFactory = jiraPropertiesFactory;
        this.gson = gson;
    }

    @Override
    protected void executeTaskImplementation() {
        logger.info("Jira Cloud property migrator task started.");
        try {
            JiraCloudProperties jiraProperties = jiraPropertiesFactory.createJiraProperties();
            JiraCloudServiceFactory serviceFactory = jiraProperties.createJiraServicesCloudFactory(logger, gson);
            IssueSearchService issueSearchService = serviceFactory.createIssueSearchService();
            IssuePropertyService issuePropertyService = serviceFactory.createIssuePropertyService();

            int startOffset = 0;
            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, startOffset, JQL_QUERY_MAX_RESULTS);
            while(startOffset <= responseModel.getTotal()) {
                List<IssueResponseModel> issueList = responseModel.getIssues();
                for (IssueResponseModel issue : issueList) {
                    String issueKey = issue.getKey();
                    IssuePropertyResponseModel issuePropertyResponse = issuePropertyService.getProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_OLD_KEY);
                    issuePropertyService.setProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, gson.toJson(issuePropertyResponse.getValue()));
                }
                startOffset += JQL_QUERY_MAX_RESULTS;
                responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, startOffset, JQL_QUERY_MAX_RESULTS);
            }

            responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, 0, 1);
            if(responseModel.getTotal() <= 0) {
                unScheduleTask();
            }

            logger.info("Jira Cloud property migrator task ended.");
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Server Configuration.", e);
        }
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION;
    }
}

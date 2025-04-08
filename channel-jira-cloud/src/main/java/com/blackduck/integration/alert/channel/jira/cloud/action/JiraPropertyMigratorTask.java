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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, 0, JQL_QUERY_MAX_RESULTS);
            if(responseModel.getTotal() > 0) {
                List<IssueResponseModel> issueList = responseModel.getIssues();
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                for (IssueResponseModel issue : issueList) {
                    String issueKey = issue.getKey();
                    executorService.submit(() -> setIssueProperty(issueKey, issuePropertyService));
                }
                executorService.shutdown();
                boolean success = executorService.awaitTermination(1, TimeUnit.MINUTES);
                if (success) {
                    logger.info("Jira Cloud property migrator task remaining issues {} ", responseModel.getTotal());
                } else {
                    logger.info("Jira Cloud property migrator task timed out updating issues; will resume with the next iteration.");
                }
            } else {
                unScheduleTask();
            }
            logger.info("Jira Cloud property migrator task ended.");
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Server Configuration.", e);
        } catch (InterruptedException e) {
            logger.error("Error updating Jira Server issues with new property.", e);
            Thread.currentThread().interrupt();
        }
    }

    private void setIssueProperty(String issueKey, IssuePropertyService issuePropertyService) {
        try {
            String propertyValue = getCurrentPropertyValue(issueKey, issuePropertyService);
            issuePropertyService.setProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_KEY, propertyValue);
        } catch (IntegrationException ex) {
            logger.error("Error migrating issue property for issue: {} cause: {}", issueKey, ex.getMessage());
            logger.debug("Caused by: ", ex);
        }
    }

    private String getCurrentPropertyValue(String issueKey, IssuePropertyService issuePropertyService) {
        // empty property value
        String jsonPropertyValue = gson.toJson(EMPTY_SEARCH_PROPERTIES);
        try {
            IssuePropertyResponseModel issuePropertyResponse = issuePropertyService.getProperty(issueKey, JiraConstants.JIRA_ISSUE_PROPERTY_OLD_KEY);
            jsonPropertyValue = gson.toJson(issuePropertyResponse.getValue());
        } catch (IntegrationException ex) {
            logger.debug("Error old issue property for issue: {} cause: {}", issueKey, ex.getMessage());
            logger.debug("Caused by: ", ex);
        }
        return jsonPropertyValue;
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_30_SECONDS_CRON_EXPRESSION;
    }
}

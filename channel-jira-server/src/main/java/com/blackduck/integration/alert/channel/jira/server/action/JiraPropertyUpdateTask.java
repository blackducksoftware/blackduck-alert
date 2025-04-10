package com.blackduck.integration.alert.channel.jira.server.action;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerProperties;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JiraPropertyUpdateTask extends JiraTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JiraServerPropertiesFactory jiraPropertiesFactory;
    private final Gson gson;

    public JiraPropertyUpdateTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraServerPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix) {
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
            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION, 0, JQL_QUERY_MAX_RESULTS);

            if(responseModel.getTotal() > 0) {
                List<IssueSearchIssueComponent> issueList = responseModel.getIssues();
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                for (IssueSearchIssueComponent issue : issueList) {
                    String issueKey = issue.getKey();
                    executorService.submit(() -> setIssueProperty(issueKey, issuePropertyService));
                }
                executorService.shutdown();
                boolean success = executorService.awaitTermination(1, TimeUnit.MINUTES);
                if (success) {
                    logger.info("Jira Server property migrator task remaining issues {} ", responseModel.getTotal());
                } else {
                    logger.info("Jira Server property migrator task timed out updating issues; will resume with the next iteration.");
                }
            } else {
                unScheduleTask();
            }
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Server Configuration.", e);
        } catch (InterruptedException e) {
            logger.error("Error updating Jira Server issues with new property.", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Jira Server property migrator task ended.");
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

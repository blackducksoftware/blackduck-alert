package com.blackduck.integration.alert.channel.jira.cloud.task;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudProperties;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.jira.common.cloud.model.IssueSearchResponseModel;
import com.blackduck.integration.jira.common.cloud.service.IssueSearchService;
import com.blackduck.integration.jira.common.cloud.service.JiraCloudServiceFactory;
import com.blackduck.integration.jira.common.model.response.IssueResponseModel;
import com.blackduck.integration.jira.common.rest.service.IssuePropertyService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class JiraSearchCommentUpdateTask extends JiraTask {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraCloudPropertiesFactory jiraPropertiesFactory;

    public JiraSearchCommentUpdateTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraCloudPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix) {
        super(taskScheduler, taskManager, configId, configName, taskNameSuffix, gson);
        this.jiraPropertiesFactory = jiraPropertiesFactory;
    }

    @Override
    public void runTask() {
        logger.info("Jira Cloud search comment task started.");
        try {
            JiraCloudProperties jiraProperties = jiraPropertiesFactory.createJiraProperties();
            JiraCloudServiceFactory serviceFactory = jiraProperties.createJiraServicesCloudFactory(logger, getGson());
            IssueSearchService issueSearchService = serviceFactory.createIssueSearchService();
            IssuePropertyService issuePropertyService = serviceFactory.createIssuePropertyService();

            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JiraConstants.JQL_QUERY_FOR_ISSUE_SEARCH_COMMENT_MIGRATION, 0, JQL_QUERY_MAX_RESULTS);

            int totalIssues = responseModel.getTotal();
            boolean foundIssues = totalIssues > 0;

            if(foundIssues) {
                List<String> issueKeys = responseModel.getIssues().stream()
                        .map(IssueResponseModel::getKey)
                        .toList();
                ExecutorService executorService = getExecutorService();
                for (String issueKey : issueKeys) {
                    executorService.submit(createUpdateRunnable(issueKey, issuePropertyService));
                }
                executorService.shutdown();
                boolean success = executorService.awaitTermination(1, TimeUnit.MINUTES);
                if (success) {
                    logger.info("Jira search key comment update task remaining issues {} ", totalIssues);
                } else {
                    logger.info("Jira search key comment update task timed out updating issues; will resume with the next iteration.");
                }
            } else {
                // unschedule the task because the query returned no results
                unScheduleTask();
            }
            logger.info("Jira Cloud search comment task ended.");
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Cloud Configuration.", e);
        } catch (InterruptedException e) {
            logger.error("Error updating Jira Cloud issues with new search key comment.", e);
            Thread.currentThread().interrupt();
        }
    }

    private Runnable createUpdateRunnable(String issueKey, IssuePropertyService issuePropertyService) {
        return () -> {};
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_30_SECONDS_CRON_EXPRESSION;
    }
}

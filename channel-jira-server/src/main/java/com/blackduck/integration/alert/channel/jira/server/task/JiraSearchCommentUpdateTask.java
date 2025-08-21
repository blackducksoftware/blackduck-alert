package com.blackduck.integration.alert.channel.jira.server.task;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class JiraSearchCommentUpdateTask extends JiraTask {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraServerPropertiesFactory jiraPropertiesFactory;

    public JiraSearchCommentUpdateTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraServerPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix) {
        super(taskScheduler, taskManager, configId, configName, taskNameSuffix, gson);
        this.jiraPropertiesFactory = jiraPropertiesFactory;
    }

    @Override
    public void runTask() {
        logger.info("Jira Server search comment task started.");
        UUID configId = UUID.fromString(getConfigId());
        try {
            JiraServerProperties jiraProperties = jiraPropertiesFactory.createJiraProperties(configId);
            JiraServerServiceFactory serviceFactory = jiraProperties.createJiraServicesServerFactory(logger,getGson());
            IssueSearchService issueSearchService = serviceFactory.createIssueSearchService();
            IssuePropertyService issuePropertyService = serviceFactory.createIssuePropertyService();
            IssueSearchResponseModel responseModel = issueSearchService.queryForIssuePage(JiraConstants.JQL_QUERY_FOR_ISSUE_SEARCH_COMMENT_MIGRATION, 0, JQL_QUERY_MAX_RESULTS);

            int totalIssues = responseModel.getTotal();
            boolean foundIssues = totalIssues > 0;

            if(foundIssues) {
                List<String> issueKeys = responseModel.getIssues().stream()
                        .map(IssueSearchIssueComponent::getKey)
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
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Server Configuration.", e);
        } catch (InterruptedException e) {
            logger.error("Error updating Jira Server issues with new search key comment.", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Jira Server search comment task ended.");
    }

    private Runnable createUpdateRunnable(String issueKey, IssuePropertyService issuePropertyService) {
        return () -> {};
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_30_SECONDS_CRON_EXPRESSION;
    }
}

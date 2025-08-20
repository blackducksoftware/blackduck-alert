package com.blackduck.integration.alert.channel.jira.server.task;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.Set;
import java.util.UUID;

public class JiraSearchCommentUpdateTask extends JiraTask {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraServerPropertiesFactory jiraPropertiesFactory;
    private final Set<String> projectNamesOrKeys;

    public JiraSearchCommentUpdateTask(TaskScheduler taskScheduler, TaskManager taskManager, JiraServerPropertiesFactory jiraPropertiesFactory, Gson gson, String configId, String configName, String taskNameSuffix, Set<String> projectNamesOrKeys) {
        super(taskScheduler, taskManager, configId, configName, taskNameSuffix, gson);
        this.jiraPropertiesFactory = jiraPropertiesFactory;
        this.projectNamesOrKeys = projectNamesOrKeys;
    }

    @Override
    protected void executeTaskImplementation() {
        logger.info("Jira Server search comment task started.");
        UUID configId = UUID.fromString(getConfigId());
        try {
        } catch (IntegrationException e) {
            logger.error("Error getting Jira Server Configuration.", e);
        } catch (InterruptedException e) {
            logger.error("Error updating Jira Server issues with new search key comment.", e);
            Thread.currentThread().interrupt();
        }

        logger.info("Jira Server property migrator task ended.");
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_30_SECONDS_CRON_EXPRESSION;
    }
}

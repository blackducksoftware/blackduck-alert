package com.blackduck.integration.alert.channel.jira.server.action;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.UUID;

public class JiraPropertyMigratorTask extends JiraTask {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public JiraPropertyMigratorTask(TaskScheduler taskScheduler, UUID configId, String configName, String taskNameSuffix) {
        super(taskScheduler, configId, configName, taskNameSuffix);
    }

    @Override
    protected void executeTaskImplementation() {
        logger.info("Jira Cloud property migrator task started.");

        logger.info("Jira Cloud property migrator task ended.");
    }

    @Override
    public String scheduleCronExpression() {
        return ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION;
    }
}

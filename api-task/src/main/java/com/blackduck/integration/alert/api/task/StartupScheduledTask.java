/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

public abstract class StartupScheduledTask extends ScheduledTask {
    private final Logger logger = LoggerFactory.getLogger(StartupScheduledTask.class);

    private final TaskManager taskManager;
    private boolean enabled;

    protected StartupScheduledTask(TaskScheduler taskScheduler, TaskManager taskManager) {
        super(taskScheduler);
        this.taskManager = taskManager;
        this.enabled = true;
    }

    public void checkTaskEnabled() {
        enabled = true;
    }

    public void startTask() {
        checkTaskEnabled();
        String taskName = getTaskName();
        if (!getEnabled()) {
            logger.info("{} is disabled and will not be scheduled to run.", taskName);
            return;
        }
        taskManager.registerTask(this);
        taskManager.scheduleCronTask(scheduleCronExpression(), taskName);
        String nextRun = taskManager.getNextRunTime(taskName).orElse("");
        logger.info("{} next run:     {}", taskName, nextRun);
        postTaskStartup();
    }

    protected void postTaskStartup() {

    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}

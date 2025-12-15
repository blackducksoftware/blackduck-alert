/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.lifecycle;

import com.blackduck.integration.alert.api.task.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JiraSchedulingManager {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TaskManager taskManager;

    @Autowired
    public JiraSchedulingManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public List<JiraTask> scheduleTasks(List<JiraTask> tasks) {
        List<JiraTask> acceptedTasks = new ArrayList<>();
        for (JiraTask task : tasks) {
            unscheduleTasks(task.getConfigId());
            logger.debug("Perform scheduling jira tasks for config with id {} and name {}", task.getConfigId(), task.getConfigName());
            if (taskManager.getNextRunTime(task.getTaskName()).isEmpty()) {
                scheduleTask(task);
                acceptedTasks.add(task);
            }
            logger.debug("Finished scheduling jira tasks for config with id {} and name {}", task.getConfigId(), task.getConfigName());
        }

        return acceptedTasks;
    }

    public void unscheduleTasks(String configId) {
        logger.debug("Performing unscheduling jira tasks for config: id={}", configId);

        List<JiraPropertyTask> tasks = taskManager.getTasksByClass(JiraPropertyTask.class)
                .stream()
                .filter(task -> task.getConfigId().equals(configId))
                .toList();

        for (JiraPropertyTask task : tasks) {
            unscheduleTask(task);
        }
        logger.debug("Finished unscheduling jira tasks for config: id={}", configId);
    }

    private void scheduleTask(JiraTask task) {
        taskManager.registerTask(task);
        taskManager.scheduleCronTask(task.scheduleCronExpression(), task.getTaskName());
    }

    private void unscheduleTask(JiraTask task) {
        taskManager.unregisterTask(task.getTaskName());
    }
}

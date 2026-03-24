/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.jira.lifecycle;

import com.blackduck.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.api.task.TaskMetaData;
import com.blackduck.integration.alert.api.task.TaskMetaDataProperty;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class JiraTask extends ScheduledTask {
    // if the task fails 120 consecutive times equaling to approximately being down for an hour consecutively then stop the task.
    public static final int FAILURE_THRESHOLD = 120;
    protected static final int JQL_QUERY_MAX_RESULTS = 100;
    protected static final JiraIssueSearchProperties EMPTY_SEARCH_PROPERTIES = new JiraIssueSearchProperties(StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    private final Logger logger = LoggerFactory.getLogger(JiraTask.class);
    private final TaskManager taskManager;
    private final String taskName;
    private final String configId;
    private final String configName;
    private final Gson gson;
    private int consecutiveFailures;

    protected JiraTask(TaskScheduler taskScheduler, TaskManager taskManager, String configId, String configName, String taskNameSuffix, Gson gson) {
        super(taskScheduler);
        this.taskManager = taskManager;
        this.configId = configId;
        this.configName = configName;
        this.taskName = computeTaskName(taskNameSuffix);
        this.gson = gson;
        this.consecutiveFailures = 0;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public TaskMetaData createTaskMetaData() {
        String fullyQualifiedName = ScheduledTask.computeFullyQualifiedName(getClass());
        String nextRunTime = getFormatedNextRunTime().orElse("");
        TaskMetaDataProperty configNameProperty = new TaskMetaDataProperty("configurationName", "Configuration Name", getConfigName());
        List<TaskMetaDataProperty> properties = List.of(configNameProperty);
        return new TaskMetaData(getTaskName(), getClass().getSimpleName(), fullyQualifiedName, nextRunTime, properties);
    }

    public String getConfigId() {
        return configId;
    }

    public String getConfigName() {
        return configName;
    }

    protected Gson getGson() {
        return gson;
    }

    protected ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public void unScheduleTask() {
        taskManager.unScheduleTask(taskName);
        taskManager.unregisterTask(taskName);
    }

    protected void resetConsecutiveFailures() {
        consecutiveFailures = 0;
    }

    protected void incrementConsecutiveFailures() {
        consecutiveFailures++;
    }

    protected void checkThresholdAndIncrementFailures() {
        if (consecutiveFailures >= FAILURE_THRESHOLD) {
            logger.info("Jira Task {} has exceeded consecutive failure threshold of {}.", taskName, FAILURE_THRESHOLD);
            unScheduleTask();
        } else {
            logger.info("Jira Task {} has failed {} out of the {} allowed.", taskName, consecutiveFailures, FAILURE_THRESHOLD);
            incrementConsecutiveFailures();
        }
    }

    private String computeTaskName(String taskNameSuffix) {
        String superTaskName = ScheduledTask.computeTaskName(getClass());
        return String.format("%s::%s[id:%s]", superTaskName, taskNameSuffix,getConfigId());
    }
}

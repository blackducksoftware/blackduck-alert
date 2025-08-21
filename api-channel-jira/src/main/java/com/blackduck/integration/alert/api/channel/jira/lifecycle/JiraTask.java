package com.blackduck.integration.alert.api.channel.jira.lifecycle;

import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.api.task.TaskMetaData;
import com.blackduck.integration.alert.api.task.TaskMetaDataProperty;
import com.google.gson.Gson;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class JiraTask extends ScheduledTask {
    protected static final int JQL_QUERY_MAX_RESULTS = 100;
    private final TaskManager taskManager;
    private final String taskName;
    private final String configId;
    private final String configName;
    private final Gson gson;

    public JiraTask(TaskScheduler taskScheduler, TaskManager taskManager, String taskName, String configId, String configName, Gson gson) {
        super(taskScheduler);
        this.taskManager = taskManager;
        this.taskName = taskName;
        this.configId = configId;
        this.configName = configName;
        this.gson = gson;
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

    public void unScheduleTask() {
        taskManager.unScheduleTask(taskName);
        taskManager.unregisterTask(taskName);
    }

    private String computeTaskName(String taskNameSuffix) {
        String superTaskName = ScheduledTask.computeTaskName(getClass());
        return String.format("%s::%s[id:%s]", superTaskName, taskNameSuffix,getConfigId());
    }
}

package com.blackduck.integration.alert.api.channel.jira.lifecycle;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.api.task.TaskMetaData;
import com.blackduck.integration.alert.api.task.TaskMetaDataProperty;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;

public abstract class JiraTask extends ScheduledTask {
    //protected static final String JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION = String.format("issue.property[%s].topicName IS NOT EMPTY AND issue.property[%s].topicName IS EMPTY ORDER BY createdDate DESC", JiraConstants.JIRA_ISSUE_PROPERTY_OLD_KEY, JiraConstants.JIRA_ISSUE_PROPERTY_KEY);
    // summary ~ "Alert " OR comment ~ "This issue was automatically created by Alert." ORDER BY  created DESC
    protected static final String JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION = String.format("(summary ~ \"Alert \" OR comment ~ \"This issue was automatically created by Alert.\") AND issue.property[%s].topicName IS EMPTY ORDER BY created DESC", JiraConstants.JIRA_ISSUE_PROPERTY_KEY);
    protected static final int JQL_QUERY_MAX_RESULTS = 100;
    private final TaskManager taskManager;
    private final String taskName;
    private final String configId;
    private final String configName;

    protected JiraTask(TaskScheduler taskScheduler, TaskManager taskManager, String configId, String configName, String taskNameSuffix) {
        super(taskScheduler);
        this.taskManager = taskManager;
        this.configId = configId;
        this.configName = configName;
        this.taskName = computeTaskName(taskNameSuffix);
    }

    @Override
    public final void runTask() {
        executeTaskImplementation();
    }

    protected abstract void executeTaskImplementation();

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public TaskMetaData createTaskMetaData() {
        String fullyQualifiedName = ScheduledTask.computeFullyQualifiedName(getClass());
        String nextRunTime = getFormatedNextRunTime().orElse("");
        TaskMetaDataProperty configIdProperty = new TaskMetaDataProperty("configId", "Configuration Id", getConfigId().toString());
        TaskMetaDataProperty configNameProperty = new TaskMetaDataProperty("configurationName", "Configuration Name", getConfigName());
        List<TaskMetaDataProperty> properties = List.of(configIdProperty, configNameProperty);
        return new TaskMetaData(getTaskName(), getClass().getSimpleName(), fullyQualifiedName, nextRunTime, properties);
    }

    public String getConfigId() {
        return configId;
    }

    public String getConfigName() {
        return configName;
    }

    public void unScheduleTask() {
        taskManager.unScheduleTask(taskName);
        scheduleExecutionAtFixedRate(0);
    }

    private String computeTaskName(String taskNameSuffix) {
        String superTaskName = ScheduledTask.computeTaskName(getClass());
        return String.format("%s::%s[id:%s]", superTaskName, taskNameSuffix,getConfigId());
    }
}

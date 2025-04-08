package com.blackduck.integration.alert.api.channel.jira.lifecycle;

import com.blackduck.integration.alert.api.channel.jira.JiraConstants;
import com.blackduck.integration.alert.api.channel.jira.JiraIssueSearchProperties;
import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.api.task.TaskMetaData;
import com.blackduck.integration.alert.api.task.TaskMetaDataProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.TaskScheduler;

import java.util.List;

public abstract class JiraTask extends ScheduledTask {
    // find tickets created by alert first:
    // 1. A summary that starts with "Alert - Black Duck"
    // 2. A summary that isn't an Alert test message and have a comment "This issue was automatically created by Alert."
    // 3. Then check if the new property key exists on that issue. Only works because the new property key is indexed with the new plugin.
    protected static final String JQL_QUERY_FOR_ISSUE_PROPERTY_MIGRATION = String.format("(summary ~ \"Alert - Black Duck\" OR (summary !~ \"Alert Test Message\" AND comment ~ \"This issue was automatically created by Alert.\")) AND issue.property[%s].topicName IS EMPTY ORDER BY created DESC", JiraConstants.JIRA_ISSUE_PROPERTY_KEY);
    protected static final int JQL_QUERY_MAX_RESULTS = 100;
    protected static final JiraIssueSearchProperties EMPTY_SEARCH_PROPERTIES = new JiraIssueSearchProperties(StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
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
        taskManager.unregisterTask(taskName);
    }

    private String computeTaskName(String taskNameSuffix) {
        String superTaskName = ScheduledTask.computeTaskName(getClass());
        return String.format("%s::%s[id:%s]", superTaskName, taskNameSuffix,getConfigId());
    }
}

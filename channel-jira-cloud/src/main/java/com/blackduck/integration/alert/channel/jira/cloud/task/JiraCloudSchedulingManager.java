package com.blackduck.integration.alert.channel.jira.cloud.task;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraSchedulingManager;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.cloud.action.JiraPropertyMigratorTask;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JiraCloudSchedulingManager {
    private final JiraSchedulingManager jiraSchedulingManager;
    private final TaskScheduler taskScheduler;
    private final TaskManager taskManager;
    private final JiraCloudPropertiesFactory jiraPropertiesFactory;
    private final Gson gson;

    @Autowired
    public JiraCloudSchedulingManager(Gson gson, JiraSchedulingManager jiraSchedulingManager, TaskScheduler taskScheduler, TaskManager taskManager, JiraCloudPropertiesFactory jiraPropertiesFactory) {
        this.gson = gson;
        this.jiraSchedulingManager = jiraSchedulingManager;
        this.taskScheduler = taskScheduler;
        this.taskManager = taskManager;
        this.jiraPropertiesFactory = jiraPropertiesFactory;
    }

    public List<JiraTask> scheduleTasks(FieldModel fieldModel) {
        return jiraSchedulingManager.scheduleTasks(createTasks(fieldModel));
    }

    public void unscheduleTasks(String configId) {
        jiraSchedulingManager.unscheduleTasks(configId);
    }

    private List<JiraTask> createTasks(FieldModel fieldModel) {
        String configId = fieldModel.getId();
        String configName = fieldModel.getFieldValue(ChannelDescriptor.KEY_NAME).orElse("");
        JiraPropertyMigratorTask task = new JiraPropertyMigratorTask(taskScheduler, taskManager, jiraPropertiesFactory, gson, configId, configName, "JiraCloud");
        return List.of(task);
    }
}

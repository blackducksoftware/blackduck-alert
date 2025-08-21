package com.blackduck.integration.alert.channel.jira.cloud.task;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraSchedulingManager;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraPropertyTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.cloud.JiraCloudPropertiesFactory;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

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

    public List<JiraPropertyTask> scheduleTasks(FieldModel fieldModel) {
        return scheduleTasks(fieldModel, Set.of());
    }

    public List<JiraPropertyTask> scheduleTasks(FieldModel fieldModel, Set<String> projectNameOrKeys) {
        return jiraSchedulingManager.scheduleTasks(createTasks(fieldModel, projectNameOrKeys));
    }

    public void unscheduleTasks(String configId) {
        jiraSchedulingManager.unscheduleTasks(configId);
    }

    private List<JiraPropertyTask> createTasks(FieldModel fieldModel, Set<String> projectNameOrKeys) {
        String configId = fieldModel.getId();
        String configName = fieldModel.getFieldValue(ChannelDescriptor.KEY_NAME).orElse("");
        JiraPropertyUpdateTask propertyTask = new JiraPropertyUpdateTask(taskScheduler, taskManager, jiraPropertiesFactory, gson, configId, configName, "JiraCloud", projectNameOrKeys);
        JiraSearchCommentUpdateTask searchCommentTask = new JiraSearchCommentUpdateTask(taskScheduler, taskManager, jiraPropertiesFactory, gson, configId, configName, "JiraCloud", projectNameOrKeys);
        return List.of(propertyTask, searchCommentTask);
    }
}

package com.blackduck.integration.alert.channel.jira.cloud.task;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraSchedulingManager;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.channel.jira.cloud.action.JiraPropertyMigratorTask;
import com.blackduck.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JiraCloudSchedulingManager {
    private JiraSchedulingManager jiraSchedulingManager;
    private TaskScheduler taskScheduler;

    @Autowired
    public JiraCloudSchedulingManager(JiraSchedulingManager jiraSchedulingManager, TaskScheduler taskScheduler) {
        this.jiraSchedulingManager = jiraSchedulingManager;
        this.taskScheduler = taskScheduler;
    }

    public List<JiraTask> scheduleTasks(FieldModel fieldModel) {
        return jiraSchedulingManager.scheduleTasks(createTasks(fieldModel));
    }

    public void unscheduleTasks(UUID configId) {
        jiraSchedulingManager.unscheduleTasks(configId);
    }

    private List<JiraTask> createTasks(FieldModel fieldModel) {
        UUID configId = UUID.fromString(fieldModel.getId());
        String configName = fieldModel.getFieldValue(ChannelDescriptor.KEY_NAME).orElse("");
        JiraPropertyMigratorTask task = new JiraPropertyMigratorTask(taskScheduler,configId, configName, "JiraCloud");
        return List.of(task);
    }
}

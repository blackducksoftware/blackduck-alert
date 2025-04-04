package com.blackduck.integration.alert.channel.jira.server.task;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraSchedulingManager;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.channel.jira.server.action.JiraPropertyMigratorTask;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.blackduck.integration.alert.common.rest.model.FieldModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class JiraServerSchedulingManager {
    private final JiraSchedulingManager jiraSchedulingManager;
    private final TaskScheduler taskScheduler;

    @Autowired
    public JiraServerSchedulingManager(JiraSchedulingManager jiraSchedulingManager, TaskScheduler taskScheduler) {
        this.jiraSchedulingManager = jiraSchedulingManager;
        this.taskScheduler = taskScheduler;
    }

    public List<JiraTask> scheduleTasks(JiraServerGlobalConfigModel configModel) {
        return jiraSchedulingManager.scheduleTasks(createTasks(configModel));
    }

    public void unscheduleTasks(UUID configId) {
        jiraSchedulingManager.unscheduleTasks(configId.toString());
    }

    private List<JiraTask> createTasks(JiraServerGlobalConfigModel configModel) {
        JiraPropertyMigratorTask task = new JiraPropertyMigratorTask(taskScheduler,configModel.getId(), configModel.getName(), "JiraServer");
        return List.of(task);
    }
}

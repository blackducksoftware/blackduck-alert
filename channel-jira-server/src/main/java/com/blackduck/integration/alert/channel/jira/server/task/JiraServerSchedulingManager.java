/*
 * blackduck-alert
 *
 * Copyright (c) 2025 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.jira.server.task;

import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraSchedulingManager;
import com.blackduck.integration.alert.api.channel.jira.lifecycle.JiraTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.channel.jira.server.JiraServerPropertiesFactory;
import com.blackduck.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class JiraServerSchedulingManager {
    private final Gson gson;
    private final JiraSchedulingManager jiraSchedulingManager;
    private final TaskScheduler taskScheduler;
    private final TaskManager taskManager;
    private final JiraServerPropertiesFactory jiraPropertiesFactory;

    @Autowired
    public JiraServerSchedulingManager(Gson gson, JiraSchedulingManager jiraSchedulingManager, TaskScheduler taskScheduler, TaskManager taskManager, JiraServerPropertiesFactory jiraPropertiesFactory) {
        this.gson = gson;
        this.jiraSchedulingManager = jiraSchedulingManager;
        this.taskManager = taskManager;
        this.taskScheduler = taskScheduler;
        this.jiraPropertiesFactory = jiraPropertiesFactory;
    }

    public List<JiraTask> scheduleTasks(JiraServerGlobalConfigModel configModel) {
        return scheduleTasks(configModel, Set.of());
    }

    public List<JiraTask> scheduleTasks(JiraServerGlobalConfigModel configModel, Set<String> projectNameOrKeys) {
        return jiraSchedulingManager.scheduleTasks(createTasks(configModel, projectNameOrKeys));
    }

    public void unscheduleTasks(UUID configId) {
        jiraSchedulingManager.unscheduleTasks(configId.toString());
    }

    private List<JiraTask> createTasks(JiraServerGlobalConfigModel configModel, Set<String> projectNameOrKeys) {
        JiraPropertyUpdateTask task = new JiraPropertyUpdateTask(taskScheduler, taskManager, jiraPropertiesFactory, gson, configModel.getId(), configModel.getName(), "JiraServer", projectNameOrKeys);
        return List.of(task);
    }
}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.tasks.web;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.api.task.TaskMetaData;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.tasks.TaskManagementDescriptorKey;

@Component
public class TaskActions {
    private final TaskManagementDescriptorKey descriptorKey;
    private final AuthorizationManager authorizationManager;
    private final TaskManager taskManager;

    @Autowired
    public TaskActions(TaskManagementDescriptorKey descriptorKey, AuthorizationManager authorizationManager, TaskManager taskManager) {
        this.descriptorKey = descriptorKey;
        this.authorizationManager = authorizationManager;
        this.taskManager = taskManager;
    }

    public ActionResponse<MultiTaskMetaDataModel> getTasks() {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey)) {
            return new ActionResponse<>(HttpStatus.FORBIDDEN, ActionResponse.FORBIDDEN_MESSAGE);
        }

        Collection<ScheduledTask> tasks = taskManager.getRunningTasks();
        List<TaskMetaData> taskList = tasks.stream()
                                          .map(ScheduledTask::createTaskMetaData)
                                          .sorted(Comparator.comparing(TaskMetaData::getType))
                                          .collect(Collectors.toList());
        MultiTaskMetaDataModel content = new MultiTaskMetaDataModel(taskList);
        return new ActionResponse<>(HttpStatus.OK, content);
    }

}

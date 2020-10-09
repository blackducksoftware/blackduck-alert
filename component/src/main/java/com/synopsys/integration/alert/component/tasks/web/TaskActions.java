/**
 * component
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.component.tasks.web;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.common.workflow.task.TaskMetaData;
import com.synopsys.integration.alert.component.tasks.TaskManagementDescriptorKey;

@Component
public class TaskActions {
    private TaskManagementDescriptorKey descriptorKey;
    private AuthorizationManager authorizationManager;
    private final TaskManager taskManager;

    @Autowired
    public TaskActions(TaskManagementDescriptorKey descriptorKey, AuthorizationManager authorizationManager, TaskManager taskManager) {
        this.descriptorKey = descriptorKey;
        this.authorizationManager = authorizationManager;
        this.taskManager = taskManager;
    }

    public ActionResponse<MultiTaskMetaDataModel> getTasks() {
        if (!authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey())) {
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

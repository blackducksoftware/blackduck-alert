/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.tasks.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.blackduck.integration.alert.api.task.ScheduledTask;
import com.blackduck.integration.alert.api.task.TaskManager;
import com.blackduck.integration.alert.api.task.TaskMetaData;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.tasks.TaskManagementDescriptorKey;

class TaskActionTest {

    @Test
    void testReadTasks() {
        Long expectedDelay = 1000L;
        TaskScheduler scheduler = Mockito.mock(TaskScheduler.class);
        ScheduledFuture scheduledFuture = Mockito.mock(ScheduledFuture.class);
        Mockito.when(scheduledFuture.isDone()).thenReturn(Boolean.FALSE);
        Mockito.when(scheduledFuture.getDelay(TimeUnit.MILLISECONDS)).thenReturn(expectedDelay);
        Mockito.when(scheduler.scheduleAtFixedRate(Mockito.any(), Mockito.anyLong())).thenReturn(scheduledFuture);
        ScheduledTask task = new ScheduledTask(scheduler) {
            @Override
            public void runTask() {

            }

            @Override
            public String scheduleCronExpression() {
                return ScheduledTask.EVERY_MINUTE_CRON_EXPRESSION;
            }
        };

        TaskManagementDescriptorKey descriptorKey = new TaskManagementDescriptorKey();
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey))
            .thenReturn(Boolean.TRUE);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);
        taskManager.scheduleExecutionAtFixedRate(expectedDelay, task.getTaskName());
        TaskActions actions = new TaskActions(descriptorKey, authorizationManager, taskManager);
        ActionResponse<MultiTaskMetaDataModel> response = actions.getTasks();
        assertTrue(response.isSuccessful());
        assertTrue(response.hasContent());
        MultiTaskMetaDataModel tasksModel = response.getContent().orElse(new MultiTaskMetaDataModel(List.of()));
        TaskMetaData model = tasksModel.getTasks().stream()
                                 .findFirst()
                                 .orElse(null);
        assertNotNull(model);
        assertNotNull(task.getTaskName());
        assertEquals(task.getFormatedNextRunTime().orElse(""), model.getNextRunTime());
    }

    @Test
    void testReadForbiddenTasks() {
        TaskManagementDescriptorKey descriptorKey = new TaskManagementDescriptorKey();
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.when(authorizationManager.hasReadPermission(ConfigContextEnum.GLOBAL, descriptorKey))
            .thenReturn(Boolean.FALSE);

        TaskManager taskManager = new TaskManager();
        TaskActions actions = new TaskActions(descriptorKey, authorizationManager, taskManager);
        ActionResponse<MultiTaskMetaDataModel> response = actions.getTasks();
        assertTrue(response.isError());
        assertFalse(response.hasContent());
    }

}

package com.synopsys.integration.alert.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.common.workflow.task.TaskMetaData;
import com.synopsys.integration.alert.web.api.task.TaskActions;

public class TaskActionTest {

    @Test
    public void testReadTasks() {
        Long expectedDelay = 1000L;
        TaskScheduler scheduler = Mockito.mock(TaskScheduler.class);
        ScheduledFuture scheduledFuture = Mockito.mock(ScheduledFuture.class);
        Mockito.when(scheduledFuture.isDone()).thenReturn(Boolean.FALSE);
        Mockito.when(scheduledFuture.getDelay(Mockito.eq(TimeUnit.MILLISECONDS))).thenReturn(expectedDelay);
        Mockito.when(scheduler.scheduleAtFixedRate(Mockito.any(), Mockito.anyLong())).thenReturn(scheduledFuture);
        ScheduledTask task = new ScheduledTask(scheduler) {
            @Override
            public void runTask() {

            }
        };

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);
        taskManager.scheduleExecutionAtFixedRate(expectedDelay, task.getTaskName());
        TaskActions actions = new TaskActions(taskManager);
        Collection<TaskMetaData> tasks = actions.getTasks();
        TaskMetaData model = tasks.stream().findFirst().orElse(null);
        assertNotNull(model);
        assertNotNull(task.getTaskName());
        assertEquals(task.getFormatedNextRunTime().orElse(""), model.getNextRunTime());
    }
}

package com.synopsys.integration.alert.common.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.task.ScheduledTask;
import com.synopsys.integration.alert.api.task.TaskManager;

public class TaskManagerTest {
    @Test
    public void testRegistration() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        Mockito.when(task.getTaskName()).thenReturn(taskName);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);
        assertEquals(1, taskManager.getTaskCount());

        Optional<ScheduledTask> emptyTask = taskManager.unregisterTask("unknown_task");
        assertTrue(emptyTask.isEmpty());
        assertEquals(1, taskManager.getTaskCount());

        Optional<ScheduledTask> removedTask = taskManager.unregisterTask(taskName);
        assertTrue(removedTask.isPresent());
        assertEquals(0, taskManager.getTaskCount());
    }

    @Test
    public void testScheduleCron() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        final String cronExpression = "cron_expression";
        Mockito.when(task.getTaskName()).thenReturn(taskName);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);
        taskManager.scheduleCronTask(cronExpression, taskName);
        Mockito.verify(task).scheduleExecution(Mockito.anyString());
    }

    @Test
    public void testScheduleCronForUnknownTask() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        final String cronExpression = "cron_expression";
        Mockito.when(task.getTaskName()).thenReturn(taskName);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);

        taskManager.scheduleCronTask(cronExpression, "unknown_task");
        Mockito.verify(task, Mockito.times(0)).scheduleExecution(Mockito.anyString());
        taskManager.scheduleCronTask(cronExpression, null);
        Mockito.verify(task, Mockito.times(0)).scheduleExecution(Mockito.anyString());
    }

    @Test
    public void testScheduleFixedRate() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        final long period = 999;
        Mockito.when(task.getTaskName()).thenReturn(taskName);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);
        taskManager.scheduleExecutionAtFixedRate(period, taskName);
        Mockito.verify(task).scheduleExecutionAtFixedRate(Mockito.anyLong());
    }

    @Test
    public void testScheduleFixedRateForUnknownTask() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        final long period = 999;
        Mockito.when(task.getTaskName()).thenReturn(taskName);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);

        taskManager.scheduleExecutionAtFixedRate(period, "unknown_task");
        Mockito.verify(task, Mockito.times(0)).scheduleExecutionAtFixedRate(Mockito.anyLong());
        taskManager.scheduleExecutionAtFixedRate(period, null);
        Mockito.verify(task, Mockito.times(0)).scheduleExecutionAtFixedRate(Mockito.anyLong());
    }

    @Test
    public void testUnschedule() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        final String cronExpression = "cron_expression";
        Mockito.when(task.getTaskName()).thenReturn(taskName);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);
        taskManager.scheduleCronTask(cronExpression, taskName);
        Mockito.verify(task).scheduleExecution(Mockito.anyString());

        assertFalse(taskManager.unScheduleTask("unknown_task"));
        assertTrue(taskManager.unScheduleTask(taskName));
    }

    @Test
    public void testGetNextRunTime() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        Mockito.when(task.getTaskName()).thenReturn(taskName);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);
        taskManager.getNextRunTime(taskName);
        Mockito.verify(task).getFormatedNextRunTime();
    }

    @Test
    public void testGetNextRunTimeForUnknownTask() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        Mockito.when(task.getTaskName()).thenReturn(taskName);

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);

        taskManager.getNextRunTime("unknown_task");
        Mockito.verify(task, Mockito.times(0)).getFormatedNextRunTime();
        taskManager.getNextRunTime(null);
        Mockito.verify(task, Mockito.times(0)).getFormatedNextRunTime();
    }

    @Test
    public void testGetDifferenceToNextRunTime() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        Mockito.when(task.getTaskName()).thenReturn(taskName);
        Mockito.when(task.getMillisecondsToNextRun()).thenReturn(Optional.of(999L));

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);
        taskManager.getDifferenceToNextRun(taskName, TimeUnit.SECONDS);
        Mockito.verify(task).getMillisecondsToNextRun();
    }

    @Test
    public void testGetDifferenceToNextRunTimeForUnknownTask() {
        ScheduledTask task = Mockito.mock(ScheduledTask.class);
        final String taskName = "a_task";
        Mockito.when(task.getTaskName()).thenReturn(taskName);
        Mockito.when(task.getMillisecondsToNextRun()).thenReturn(Optional.of(999L));

        TaskManager taskManager = new TaskManager();
        taskManager.registerTask(task);

        taskManager.getDifferenceToNextRun("unknown_task", TimeUnit.SECONDS);
        Mockito.verify(task, Mockito.times(0)).getMillisecondsToNextRun();
        taskManager.getDifferenceToNextRun(null, TimeUnit.SECONDS);
        Mockito.verify(task, Mockito.times(0)).getMillisecondsToNextRun();
    }

}

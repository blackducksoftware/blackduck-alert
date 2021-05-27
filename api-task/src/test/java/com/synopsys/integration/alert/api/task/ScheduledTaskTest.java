package com.synopsys.integration.alert.api.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

public class ScheduledTaskTest {
    private final String validCronExpression = "0 0/1 * 1/1 * *";
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> future;
    private ScheduledTask task;

    @BeforeEach
    public void initializeTest() {
        taskScheduler = Mockito.mock(TaskScheduler.class);
        future = Mockito.mock(ScheduledFuture.class);
        task = new ScheduledTask(taskScheduler) {
            @Override
            public void runTask() {

            }

            @Override
            public String scheduleCronExpression() {
                return validCronExpression;
            }
        };
    }

    @Test
    public void testComputeTaskName() {
        assertEquals(ScheduledTask.computeTaskName(task.getClass()), task.getTaskName());
    }

    @Test
    public void testNextFormattedRuntime() {
        final Long millisecondsToNextRun = 10000L;
        ZonedDateTime currentUTCTime = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime expectedDateTime = currentUTCTime.plus(millisecondsToNextRun, ChronoUnit.MILLIS);
        int seconds = expectedDateTime.getSecond();
        if (seconds >= 30) {
            expectedDateTime = expectedDateTime.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        } else {
            expectedDateTime = expectedDateTime.truncatedTo(ChronoUnit.MINUTES);
        }
        String expectedNextRunTime = expectedDateTime.format(DateTimeFormatter.ofPattern(ScheduledTask.FORMAT_PATTERN)) + " UTC";

        Mockito.doReturn(future).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        Mockito.when(future.getDelay(TimeUnit.MILLISECONDS)).thenReturn(millisecondsToNextRun);
        task.scheduleExecution(validCronExpression);
        Optional<String> nextRunTime = task.getFormatedNextRunTime();
        assertTrue(nextRunTime.isPresent());
        String nextTime = nextRunTime.get();
        assertEquals(expectedNextRunTime, nextTime);
    }

    @Test
    public void testNextFormattedRuntimeNullFuture() {
        Mockito.doReturn(null).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        task.scheduleExecution(validCronExpression);
        Optional<String> nextRunTime = task.getFormatedNextRunTime();
        assertFalse(nextRunTime.isPresent());
    }

    @Test
    public void testNextFormattedRuntimeFutureCancelled() {
        Mockito.doReturn(future).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        Mockito.when(future.isCancelled()).thenReturn(true);
        task.scheduleExecution(validCronExpression);
        Optional<String> nextRunTime = task.getFormatedNextRunTime();
        assertFalse(nextRunTime.isPresent());
    }

    @Test
    public void testNextFormattedRuntimeFutureDone() {
        Mockito.doReturn(future).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        Mockito.when(future.isDone()).thenReturn(true);
        task.scheduleExecution(validCronExpression);
        Optional<String> nextRunTime = task.getFormatedNextRunTime();
        assertFalse(nextRunTime.isPresent());
    }

    @Test
    public void testNextRuntime() {
        final Long expectedNextRun = 10000L;
        Mockito.doReturn(future).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        Mockito.when(future.getDelay(TimeUnit.MILLISECONDS)).thenReturn(expectedNextRun);
        task.scheduleExecution(validCronExpression);
        Optional<Long> actualNextRun = task.getMillisecondsToNextRun();
        assertTrue(actualNextRun.isPresent());
        assertEquals(expectedNextRun, actualNextRun.get());
    }

    @Test
    public void testNextRuntimeNullFuture() {
        Mockito.doReturn(null).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        task.scheduleExecution(validCronExpression);
        Optional<Long> actualNextRun = task.getMillisecondsToNextRun();
        assertFalse(actualNextRun.isPresent());
    }

    @Test
    public void testNextRuntimeFutureCancelled() {
        Mockito.doReturn(future).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        Mockito.when(future.isCancelled()).thenReturn(true);
        task.scheduleExecution(validCronExpression);
        Optional<Long> actualNextRun = task.getMillisecondsToNextRun();
        assertFalse(actualNextRun.isPresent());
    }

    @Test
    public void testNextRuntimeFutureDone() {
        Mockito.doReturn(future).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        Mockito.when(future.isDone()).thenReturn(true);
        task.scheduleExecution(validCronExpression);
        Optional<Long> actualNextRun = task.getMillisecondsToNextRun();
        assertFalse(actualNextRun.isPresent());
    }

    @Test
    public void testScheduleCronExecution() {
        task.scheduleExecution(validCronExpression);
        Mockito.verify(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
    }

    @Test
    public void testScheduleCronExecutionBlankString() {
        task.scheduleExecution("");
        Mockito.verify(taskScheduler, Mockito.times(0)).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
    }

    @Test
    public void testScheduleCronExecutionBlankException() {
        Mockito.doThrow(new IllegalArgumentException("Argument exception")).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        task.scheduleExecution(validCronExpression);
        assertFalse(task.getMillisecondsToNextRun().isPresent());
    }

    @Test
    public void testScheduleCronExecutionUnschedule() {
        Mockito.doReturn(future).when(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        task.scheduleExecution(validCronExpression);
        Mockito.verify(taskScheduler).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
        task.scheduleExecution(ScheduledTask.STOP_SCHEDULE_EXPRESSION);
        Mockito.verify(taskScheduler, Mockito.times(1)).schedule(Mockito.any(), Mockito.any(CronTrigger.class));
    }

    @Test
    public void testScheduleFixedRateExecution() {
        task.scheduleExecutionAtFixedRate(1L);
        Mockito.verify(taskScheduler).scheduleAtFixedRate(Mockito.any(), Mockito.anyLong());
    }

    @Test
    public void testScheduleFixedRateExecutionZeroPeriod() {
        task.scheduleExecutionAtFixedRate(0L);
        Mockito.verify(taskScheduler, Mockito.times(0)).scheduleAtFixedRate(Mockito.any(), Mockito.anyLong());
    }

    @Test
    public void testScheduleFixedRateExecutionUnschedule() {
        Mockito.doReturn(future).when(taskScheduler).scheduleAtFixedRate(Mockito.any(), Mockito.anyLong());
        task.scheduleExecutionAtFixedRate(1L);
        Mockito.verify(taskScheduler).scheduleAtFixedRate(Mockito.any(), Mockito.anyLong());
        task.scheduleExecutionAtFixedRate(0L);
        Mockito.verify(taskScheduler, Mockito.times(1)).scheduleAtFixedRate(Mockito.any(), Mockito.anyLong());
    }

}


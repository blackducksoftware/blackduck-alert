package com.synopsys.integration.alert.workflow.scheduled.frequency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessorV2;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;

public class ProcessingTaskTest {
    private List<AlertNotificationModel> modelList;

    @BeforeEach
    public void initTest() {
        AlertNotificationModel model = new AlertNotificationModel(
            1L, 1L, "BlackDuck", "BlackDuck_1", "NotificationType", "{content: \"content is here\"}", DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), false);
        modelList = List.of(model);
    }

    @Test
    public void testGetTaskName() {
        ProcessingTask task = createTask(null, null, null, null);
        assertEquals(ProcessingTask.computeTaskName(task.getClass()), task.getTaskName());
    }

    @Test
    public void testDateRange() {
        ProcessingTask task = createTask(null, null, null, null);
        DateRange dateRange = task.getDateRange();
        OffsetDateTime expectedEndDay = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime expectedStartDay = task.getLastRunTime();

        ZonedDateTime actualStartDay = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        ZonedDateTime actualEndDay = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        assertDateIsEqual(expectedStartDay, actualStartDay);
        assertDateIsEqual(expectedEndDay, actualEndDay);
    }

    /* //FIXME
    @Test
    public void testRun() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);

        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(OffsetDateTime.class), Mockito.any(OffsetDateTime.class))).thenReturn(modelList);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(List.of());
        JobNotificationMapper jobNotificationMapper = Mockito.mock(JobNotificationMapper.class);
        Mockito.when(jobNotificationMapper.mapJobsToNotifications(Mockito.anyList(), Mockito.anyCollection())).thenReturn(List.of());
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        Mockito.doNothing().when(notificationAccessor).setNotificationsProcessed(Mockito.anyList());
        NotificationProcessorV2 notificationProcessor = new NotificationProcessorV2(extractionDelegator, jobNotificationMapper, null, null, List.of(), notificationAccessor);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager);
        ProcessingTask processingTask = Mockito.spy(task);

        processingTask.run();
        Mockito.verify(processingTask).getDateRange();
        Mockito.verify(processingTask).read(Mockito.any());
    }

     */

    @Test
    public void testRead() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(List.of());
        NotificationProcessorV2 notificationProcessor = new NotificationProcessorV2(extractionDelegator, null, null, null, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager);
        DateRange dateRange = task.getDateRange();
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(modelList);
        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(modelList, actualModelList);
    }

    @Test
    public void testReadEmptyList() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(List.of());
        NotificationProcessorV2 notificationProcessor = new NotificationProcessorV2(extractionDelegator, null, null, null, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager);
        DateRange dateRange = task.getDateRange();
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(Collections.emptyList());
        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    public void testReadException() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(List.of());
        NotificationProcessorV2 notificationProcessor = new NotificationProcessorV2(extractionDelegator, null, null, null, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager);
        DateRange dateRange = task.getDateRange();
        Mockito.doThrow(new RuntimeException("Exception reading data")).when(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    private void assertDateIsEqual(OffsetDateTime expected, ZonedDateTime actual) {
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }

    private ProcessingTask createTask(TaskScheduler taskScheduler, DefaultNotificationAccessor notificationManager, NotificationProcessorV2 notificationProcessor, TaskManager taskManager) {
        return new ProcessingTask(taskScheduler, notificationManager, taskManager, notificationProcessor, FrequencyType.DAILY) {
            @Override
            public String scheduleCronExpression() {
                return null;
            }

        };
    }

}

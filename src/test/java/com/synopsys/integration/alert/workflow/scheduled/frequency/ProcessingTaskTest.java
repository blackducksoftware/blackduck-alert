package com.synopsys.integration.alert.workflow.scheduled.frequency;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.common.channel.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.workflow.processor.NotificationProcessor;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.database.api.DefaultNotificationManager;
import com.synopsys.integration.rest.RestConstants;

public class ProcessingTaskTest {

    private List<AlertNotificationModel> modelList;
    private List<DistributionEvent> eventList;

    @BeforeEach
    public void initTest() {
        AlertNotificationModel model = new AlertNotificationModel(1L, 1L, "BlackDuck", "BlackDuck_1", "NotificationType", "{content: \"content is here\"}", new Date(), new Date());
        modelList = Arrays.asList(model);
        eventList = Arrays.asList(new DistributionEvent("1L", "FORMAT", RestConstants.formatDate(new Date()), "Provider", FormatType.DEFAULT.name(), null, new FieldAccessor(Map.of())));
    }

    public ProcessingTask createTask(TaskScheduler taskScheduler, DefaultNotificationManager notificationManager, NotificationProcessor notificationProcessor, ChannelEventManager eventManager,
        TaskManager taskManager) {
        return new ProcessingTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager) {

            @Override
            public String scheduleCronExpression() {
                return null;
            }

            @Override
            public FrequencyType getDigestType() {
                return FrequencyType.DAILY;
            }
        };
    }

    @Test
    public void testGetTaskName() {
        ProcessingTask task = createTask(null, null, null, null, null);
        assertEquals(ProcessingTask.computeTaskName(task.getClass()), task.getTaskName());
    }

    @Test
    public void testDateRange() {
        ProcessingTask task = createTask(null, null, null, null, null);
        DateRange dateRange = task.getDateRange();
        ZonedDateTime expectedEndDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime expectedStartDay = task.getLastRunTime();

        ZonedDateTime actualStartDay = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        ZonedDateTime actualEndDay = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        assertDateIsEqual(expectedStartDay, actualStartDay);
        assertDateIsEqual(expectedEndDay, actualEndDay);
    }

    @Test
    public void testDigestType() {
        ProcessingTask task = createTask(null, null, null, null, null);
        assertEquals(FrequencyType.DAILY, task.getDigestType());
    }

    @Test
    public void testRun() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(modelList);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager);

        ProcessingTask processingTask = Mockito.spy(task);
        processingTask.run();
        Mockito.verify(processingTask).getDateRange();
        Mockito.verify(processingTask).read(Mockito.any());
        Mockito.verify(eventManager).sendEvents(eventList);
    }

    @Test
    public void testRead() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager);
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
        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager);
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
        DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager);
        DateRange dateRange = task.getDateRange();
        Mockito.doThrow(new RuntimeException("Exception reading data")).when(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    private void assertDateIsEqual(ZonedDateTime expected, ZonedDateTime actual) {
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }
}

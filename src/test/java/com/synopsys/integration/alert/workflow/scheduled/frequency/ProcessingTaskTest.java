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

import com.synopsys.integration.alert.channel.util.ChannelEventManager;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationWrapper;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.database.api.DefaultNotificationManager;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.workflow.processor.NotificationProcessor;
import com.synopsys.integration.rest.RestConstants;

public class ProcessingTaskTest {

    private final String taskName = "processing-test-task";
    private List<AlertNotificationWrapper> modelList;
    private List<DistributionEvent> eventList;

    @BeforeEach
    public void initTest() {
        final AlertNotificationWrapper model = new MockNotificationContent(new Date(), "BlackDuck", new Date(), "NotificationType", "{content: \"content is here\"}", null).createEntity();
        modelList = Arrays.asList(model);
        eventList = Arrays.asList(new DistributionEvent("1L", "FORMAT", RestConstants.formatDate(new Date()), "Provider", FormatType.DEFAULT.name(), null, new FieldAccessor(Map.of())));
    }

    public ProcessingTask createTask(final TaskScheduler taskScheduler, final DefaultNotificationManager notificationManager, final NotificationProcessor notificationProcessor, final ChannelEventManager eventManager,
        final TaskManager taskManager) {
        return new ProcessingTask(taskScheduler, taskName, notificationManager, notificationProcessor, eventManager, taskManager) {

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
        final ProcessingTask task = createTask(null, null, null, null, null);
        assertEquals(taskName, task.getTaskName());
    }

    @Test
    public void testDateRange() {
        final ProcessingTask task = createTask(null, null, null, null, null);
        final DateRange dateRange = task.getDateRange();
        final ZonedDateTime expectedEndDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final ZonedDateTime expectedStartDay = task.getLastRunTime();

        final ZonedDateTime actualStartDay = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        final ZonedDateTime actualEndDay = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        assertDateIsEqual(expectedStartDay, actualStartDay);
        assertDateIsEqual(expectedEndDay, actualEndDay);
    }

    @Test
    public void testDigestType() {
        final ProcessingTask task = createTask(null, null, null, null, null);
        assertEquals(FrequencyType.DAILY, task.getDigestType());
    }

    @Test
    public void testRun() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        final ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(modelList);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager);

        final ProcessingTask processingTask = Mockito.spy(task);
        processingTask.run();
        Mockito.verify(processingTask).getDateRange();
        Mockito.verify(processingTask).read(Mockito.any());
        Mockito.verify(eventManager).sendEvents(eventList);
    }

    @Test
    public void testRead() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager);
        final DateRange dateRange = task.getDateRange();
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(modelList);
        final ProcessingTask processingTask = Mockito.spy(task);
        final List<AlertNotificationWrapper> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(modelList, actualModelList);
    }

    @Test
    public void testReadEmptyList() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager);
        final DateRange dateRange = task.getDateRange();
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(Collections.emptyList());
        final ProcessingTask processingTask = Mockito.spy(task);
        final List<AlertNotificationWrapper> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    public void testReadException() {
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final DefaultNotificationManager notificationManager = Mockito.mock(DefaultNotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ChannelEventManager eventManager = Mockito.mock(ChannelEventManager.class);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, eventManager, taskManager);
        final DateRange dateRange = task.getDateRange();
        Mockito.doThrow(new RuntimeException("Exception reading data")).when(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        final ProcessingTask processingTask = Mockito.spy(task);
        final List<AlertNotificationWrapper> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    private void assertDateIsEqual(final ZonedDateTime expected, final ZonedDateTime actual) {
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }
}

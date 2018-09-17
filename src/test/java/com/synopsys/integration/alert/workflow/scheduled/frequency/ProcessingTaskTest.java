package com.synopsys.integration.alert.workflow.scheduled.frequency;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.synopsys.integration.alert.channel.ChannelTemplateManager;
import com.synopsys.integration.alert.channel.event.ChannelEvent;
import com.synopsys.integration.alert.common.model.DateRange;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.workflow.NotificationManager;
import com.synopsys.integration.alert.workflow.processor.NotificationProcessor;
import com.synopsys.integration.rest.RestConstants;

public class ProcessingTaskTest {

    private final String taskName = "processing-test-task";
    private List<NotificationContent> modelList;
    private List<ChannelEvent> eventList;

    @Before
    public void initTest() {
        final NotificationContent model = new MockNotificationContent(new Date(), "BlackDuck", "NotificationType", "{content: \"content is here\"}", null).createEntity();
        modelList = Arrays.asList(model);
        eventList = Arrays.asList(new ChannelEvent("destination", RestConstants.formatDate(new Date()), "provider", "notificationType", "content", 1L, null));
    }

    public ProcessingTask createTask(final TaskScheduler taskScheduler, final NotificationManager notificationManager, final NotificationProcessor notificationProcessor, final ChannelTemplateManager channelTemplateManager) {
        return new ProcessingTask(taskScheduler, taskName, notificationManager, notificationProcessor, channelTemplateManager) {

            @Override
            public FrequencyType getDigestType() {
                return FrequencyType.DAILY;
            }
        };
    }

    @Test
    public void testGetTaskName() {
        final ProcessingTask task = createTask(null, null, null, null);
        assertEquals(taskName, task.getTaskName());
    }

    @Test
    public void testDateRange() {
        final ProcessingTask task = createTask(null, null, null, null);
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
        final ProcessingTask task = createTask(null, null, null, null);
        assertEquals(FrequencyType.DAILY, task.getDigestType());
    }

    private void assertDateIsEqual(final ZonedDateTime expected, final ZonedDateTime actual) {
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }

    @Test
    public void testRun() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(Date.class), Mockito.any(Date.class))).thenReturn(modelList);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager);

        final ProcessingTask processingTask = Mockito.spy(task);
        processingTask.run();
        Mockito.verify(processingTask).getDateRange();
        Mockito.verify(processingTask).read(Mockito.any());
        Mockito.verify(processingTask).process(modelList);
        Mockito.verify(channelTemplateManager).sendEvents(eventList);
    }

    @Test
    public void testRead() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager);
        final DateRange dateRange = task.getDateRange();
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(modelList);
        final ProcessingTask processingTask = Mockito.spy(task);
        final List<NotificationContent> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(modelList, actualModelList);
    }

    @Test
    public void testReadEmptyList() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager);
        final DateRange dateRange = task.getDateRange();
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(Collections.emptyList());
        final ProcessingTask processingTask = Mockito.spy(task);
        final List<NotificationContent> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    public void testReadException() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager);
        final DateRange dateRange = task.getDateRange();
        Mockito.doThrow(new RuntimeException("Exception reading data")).when(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        final ProcessingTask processingTask = Mockito.spy(task);
        final List<NotificationContent> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    public void testProcess() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(FrequencyType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager);
        final DateRange dateRange = task.getDateRange();
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(modelList);
        final ProcessingTask processingTask = Mockito.spy(task);
        final List<ChannelEvent> actualEventList = processingTask.process(modelList);
        assertEquals(eventList, actualEventList);
    }

    @Test
    public void testProcessEmptyList() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final NotificationProcessor notificationProcessor = Mockito.mock(NotificationProcessor.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask processingTask = Mockito.spy(createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager));
        final List<ChannelEvent> actualEventList = processingTask.process(Collections.emptyList());
        assertEquals(Collections.emptyList(), actualEventList);

    }
}

package com.blackducksoftware.integration.alert.workflow.scheduled.frequency;

import static org.junit.Assert.assertEquals;

import java.sql.Date;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.blackducksoftware.integration.alert.channel.ChannelTemplateManager;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.common.digest.DateRange;
import com.blackducksoftware.integration.alert.common.digest.DigestNotificationProcessor;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.database.entity.NotificationEntity;
import com.blackducksoftware.integration.alert.workflow.NotificationManager;

public class ProcessingTaskTest {

    private DateRange dateRange;
    private List<NotificationModel> modelList;
    private List<ChannelEvent> eventList;
    private final String taskName = "processing-test-task";

    @Before
    public void initTest() {
        final ZonedDateTime endDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final ZonedDateTime startDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).minusDays(1);
        dateRange = new DateRange(Date.from(startDay.toInstant()), Date.from(endDay.toInstant()));
        final NotificationModel model = new NotificationModel(Mockito.mock(NotificationEntity.class), Collections.emptyList());
        modelList = Arrays.asList(model);
        eventList = Arrays.asList(new ChannelEvent("destination", "content", 1L));
    }

    public ProcessingTask createTask(final TaskScheduler taskScheduler, final NotificationManager notificationManager, final DigestNotificationProcessor digestNotificationProcessor, final ChannelTemplateManager channelTemplateManager) {
        return new ProcessingTask(taskScheduler, taskName, notificationManager, digestNotificationProcessor, channelTemplateManager) {
            @Override
            public DateRange getDateRange() {
                return dateRange;
            }

            @Override
            public DigestType getDigestType() {
                return DigestType.DAILY;
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
        final ZonedDateTime expectedStartDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).minusDays(1);

        final ZonedDateTime actualStartDay = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        final ZonedDateTime actualEndDay = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        assertDateIsEqual(expectedStartDay, actualStartDay);
        assertDateIsEqual(expectedEndDay, actualEndDay);
    }

    @Test
    public void testDigestType() {
        final ProcessingTask task = createTask(null, null, null, null);
        assertEquals(DigestType.DAILY, task.getDigestType());
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
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(modelList);
        final DigestNotificationProcessor notificationProcessor = Mockito.mock(DigestNotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(DigestType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask processingTask = Mockito.spy(createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager));
        processingTask.run();
        Mockito.verify(processingTask).getDateRange();
        Mockito.verify(processingTask).read(dateRange);
        Mockito.verify(processingTask).process(modelList);
        Mockito.verify(channelTemplateManager).sendEvents(eventList);
    }

    @Test
    public void testRead() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(modelList);
        final DigestNotificationProcessor notificationProcessor = Mockito.mock(DigestNotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(DigestType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask processingTask = Mockito.spy(createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager));
        final List<NotificationModel> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(modelList, actualModelList);
    }

    @Test
    public void testReadEmptyList() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(Collections.emptyList());
        final DigestNotificationProcessor notificationProcessor = Mockito.mock(DigestNotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(DigestType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask processingTask = Mockito.spy(createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager));
        final List<NotificationModel> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    public void testReadException() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.doThrow(new RuntimeException("Exception reading data")).when(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        final DigestNotificationProcessor notificationProcessor = Mockito.mock(DigestNotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(DigestType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask processingTask = Mockito.spy(createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager));
        final List<NotificationModel> actualModelList = processingTask.read(dateRange);
        Mockito.verify(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd());
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    public void testProcess() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd())).thenReturn(modelList);
        final DigestNotificationProcessor notificationProcessor = Mockito.mock(DigestNotificationProcessor.class);
        Mockito.when(notificationProcessor.processNotifications(DigestType.DAILY, modelList)).thenReturn(eventList);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask processingTask = Mockito.spy(createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager));
        final List<ChannelEvent> actualEventList = processingTask.process(modelList);
        assertEquals(eventList, actualEventList);
    }

    @Test
    public void testProcessEmptyList() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final DigestNotificationProcessor notificationProcessor = Mockito.mock(DigestNotificationProcessor.class);
        final ChannelTemplateManager channelTemplateManager = Mockito.mock(ChannelTemplateManager.class);
        final ProcessingTask processingTask = Mockito.spy(createTask(taskScheduler, notificationManager, notificationProcessor, channelTemplateManager));
        final List<ChannelEvent> actualEventList = processingTask.process(Collections.emptyList());
        assertEquals(Collections.emptyList(), actualEventList);

    }
}

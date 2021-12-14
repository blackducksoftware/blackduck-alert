package com.synopsys.integration.alert.workflow.scheduled.frequency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
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

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.filter.FilteredJobNotificationWrapper;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.processor.api.filter.PageRetriever;
import com.synopsys.integration.alert.processor.api.filter.StatefulAlertPage;
import com.synopsys.integration.alert.provider.blackduck.task.accumulator.BlackDuckNotificationRetriever;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;

class ProcessingTaskTest {
    private final BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(new Gson());

    private List<AlertNotificationModel> modelList;

    @BeforeEach
    public void initTest() throws IOException {
        String notificationJson = TestResourceUtils.readFileToString("json/projectVersionNotification.json");
        AlertNotificationModel model = new AlertNotificationModel(
            1L, 1L, "BlackDuck", "BlackDuck_1", "PROJECT_VERSION", notificationJson, DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), false);
        modelList = List.of(model);
    }

    @Test
    void testGetTaskName() {
        ProcessingTask task = createTask(null, null, null, null, null);
        assertEquals(ProcessingTask.computeTaskName(task.getClass()), task.getTaskName());
    }

    @Test
    void testDateRange() {
        ProcessingTask task = createTask(null, null, null, null, null);
        DateRange dateRange = task.getDateRange();
        OffsetDateTime expectedEndDay = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime expectedStartDay = task.getLastRunTime();

        ZonedDateTime actualStartDay = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        ZonedDateTime actualEndDay = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        assertDateIsEqual(expectedStartDay, actualStartDay);
        assertDateIsEqual(expectedEndDay, actualEndDay);
    }

    @Test
    void testRun() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);

        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        AlertPagedModel<AlertNotificationModel> page = new AlertPagedModel<>(1, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, modelList);
        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(OffsetDateTime.class), Mockito.any(OffsetDateTime.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(page);

        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.countJobsByFrequency(Mockito.any())).thenReturn(1);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        JobNotificationMapper jobNotificationMapper = Mockito.mock(JobNotificationMapper.class);
        StatefulAlertPage<FilteredJobNotificationWrapper, RuntimeException> statefulAlertPage = new StatefulAlertPage(AlertPagedDetails.emptyPage(), Mockito.mock(PageRetriever.class), BlackDuckNotificationRetriever.HAS_NEXT_PAGE);
        Mockito.when(jobNotificationMapper.mapJobsToNotifications(Mockito.anyList(), Mockito.anyList())).thenReturn(statefulAlertPage);
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        Mockito.doNothing().when(notificationAccessor).setNotificationsProcessed(Mockito.anyList());
        NotificationProcessor notificationProcessor = new NotificationProcessor(extractionDelegator, jobNotificationMapper, null, null, List.of(), notificationAccessor);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager, jobAccessor);
        ProcessingTask processingTask = Mockito.spy(task);

        processingTask.run();
        Mockito.verify(processingTask).getDateRange();
        Mockito.verify(processingTask).read(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void testRead() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.countJobsByFrequency(Mockito.any())).thenReturn(1);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        NotificationProcessor notificationProcessor = new NotificationProcessor(extractionDelegator, null, null, null, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager, jobAccessor);
        DateRange dateRange = task.getDateRange();
        AlertPagedModel<AlertNotificationModel> page = new AlertPagedModel<>(1, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, modelList);
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd(), AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE)).thenReturn(page);
        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE).getModels();
        assertEquals(modelList, actualModelList);
    }

    @Test
    void testReadEmptyList() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.countJobsByFrequency(Mockito.any())).thenReturn(1);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        NotificationProcessor notificationProcessor = new NotificationProcessor(extractionDelegator, null, null, null, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager, jobAccessor);
        DateRange dateRange = task.getDateRange();
        AlertPagedModel<AlertNotificationModel> emptyPage = new AlertPagedModel<>(0, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, List.of());
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd(), AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE)).thenReturn(emptyPage);
        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE).getModels();
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    void testJobCountZero() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.countJobsByFrequency(Mockito.any())).thenReturn(0);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        NotificationProcessor notificationProcessor = new NotificationProcessor(extractionDelegator, null, null, null, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager, jobAccessor);
        DateRange dateRange = task.getDateRange();
        AlertPagedModel<AlertNotificationModel> emptyPage = new AlertPagedModel<>(0, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE, List.of());
        Mockito.when(notificationManager.findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd(), AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE))
            .thenReturn(emptyPage);
        ProcessingTask processingTask = Mockito.spy(task);
        processingTask.runTask();
        Mockito.verify(notificationManager, Mockito.times(0)).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd(), AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE);
    }

    @Test
    void testReadException() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        NotificationProcessor notificationProcessor = new NotificationProcessor(extractionDelegator, null, null, null, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationProcessor, taskManager, jobAccessor);
        DateRange dateRange = task.getDateRange();
        Mockito.doThrow(new RuntimeException("Exception reading data")).when(notificationManager).findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd(), AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE);
        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE).getModels();
        assertEquals(Collections.emptyList(), actualModelList);
    }

    private void assertDateIsEqual(OffsetDateTime expected, ZonedDateTime actual) {
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }

    private ProcessingTask createTask(TaskScheduler taskScheduler, DefaultNotificationAccessor notificationManager, NotificationProcessor notificationProcessor, TaskManager taskManager, JobAccessor jobAccessor) {
        return new ProcessingTask(taskScheduler, taskManager, notificationManager, notificationProcessor, jobAccessor, FrequencyType.DAILY) {
            @Override
            public String scheduleCronExpression() {
                return null;
            }

        };
    }

}

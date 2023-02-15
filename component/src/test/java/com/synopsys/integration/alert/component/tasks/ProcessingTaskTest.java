package com.synopsys.integration.alert.component.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.scheduling.TaskScheduler;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.api.StaticJobAccessor;
import com.synopsys.integration.alert.processor.api.NotificationMappingProcessor;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;

class ProcessingTaskTest {
    private final BlackDuckResponseResolver blackDuckResponseResolver = new BlackDuckResponseResolver(new Gson());

    private List<AlertNotificationModel> modelList;

    @BeforeEach
    public void initTest() throws IOException {
        String notificationJson = TestResourceUtils.readFileToString("json/projectVersionNotification.json");
        AlertNotificationModel model = new AlertNotificationModel(
            1L,
            1L,
            "BlackDuck",
            "BlackDuck_1",
            "PROJECT_VERSION",
            notificationJson,
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(),
            false,
            String.format("content-id-%s", UUID.randomUUID())
        );
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
    void testRun() throws IOException {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);

        NotificationAccessor notificationAccessor = new MockProcessingNotificationAccessor(List.of());
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.hasJobsByFrequency(Mockito.any())).thenReturn(true);

        JobNotificationMapper jobNotificationMapper = Mockito.mock(JobNotificationMapper.class);
        Mockito.when(jobNotificationMapper.mapJobsToNotifications(Mockito.anyList(), Mockito.anyList())).thenReturn(Set.of());
        NotificationMappingProcessor notificationMappingProcessor = Mockito.mock(NotificationMappingProcessor.class);

        ProcessingTask processingTask = createTask(taskScheduler, notificationAccessor, notificationMappingProcessor, taskManager, jobAccessor);
        int expectedPages = 5;
        int count = ProcessingTask.PAGE_SIZE * expectedPages;

        List<AlertNotificationModel> allModels = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            String notificationJson = TestResourceUtils.readFileToString("json/projectVersionNotification.json");
            AlertNotificationModel model = new AlertNotificationModel(
                Integer.valueOf(index).longValue(), 1L,
                "BlackDuck",
                "BlackDuck_1",
                "PROJECT_VERSION",
                notificationJson,
                DateUtils.createCurrentDateTimestamp(),
                DateUtils.createCurrentDateTimestamp(),
                false,
                String.format("content-id-%s", UUID.randomUUID())
            );
            allModels.add(model);
        }
        notificationAccessor.saveAllNotifications(allModels);
        ProcessingTask spyProcessingTask = Mockito.spy(processingTask);

        spyProcessingTask.run();
        Mockito.verify(spyProcessingTask).getDateRange();
        Mockito.verify(spyProcessingTask, Mockito.times(expectedPages + 1)).read(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    void testReadEmptyList() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        NotificationAccessor notificationManager = new MockProcessingNotificationAccessor(List.of());
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.hasJobsByFrequency(Mockito.any())).thenReturn(true);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(extractionDelegator, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationMappingProcessor, taskManager, jobAccessor);
        DateRange dateRange = task.getDateRange();

        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE).getModels();
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    void testJobCountZero() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        NotificationAccessor notificationManager = new MockProcessingNotificationAccessor(List.of());
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.hasJobsByFrequency(Mockito.any())).thenReturn(false);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(extractionDelegator, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationMappingProcessor, taskManager, jobAccessor);
        DateRange dateRange = task.getDateRange();
        ProcessingTask processingTask = Mockito.spy(task);
        processingTask.runTask();
        Mockito.verify(processingTask, Mockito.times(0)).getDateRange();
    }

    @Test
    void testReadException() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        DefaultNotificationAccessor notificationManager = Mockito.mock(DefaultNotificationAccessor.class);
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(extractionDelegator, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationMappingProcessor, taskManager, jobAccessor);
        DateRange dateRange = task.getDateRange();
        Mockito.doThrow(new RuntimeException("Exception reading data")).when(notificationManager)
            .findByCreatedAtBetween(dateRange.getStart(), dateRange.getEnd(), AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE);
        ProcessingTask processingTask = Mockito.spy(task);
        List<AlertNotificationModel> actualModelList = processingTask.read(dateRange, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE).getModels();
        assertEquals(Collections.emptyList(), actualModelList);
    }

    @Test
    void testPagedRead() throws IOException {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        NotificationAccessor notificationManager = new MockProcessingNotificationAccessor(List.of());
        StaticJobAccessor jobAccessor = Mockito.mock(StaticJobAccessor.class);
        Mockito.when(jobAccessor.hasJobsByFrequency(Mockito.any())).thenReturn(true);

        NotificationDetailExtractionDelegator extractionDelegator = new NotificationDetailExtractionDelegator(blackDuckResponseResolver, List.of());
        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(extractionDelegator, null, null);

        ProcessingTask task = createTask(taskScheduler, notificationManager, notificationMappingProcessor, taskManager, jobAccessor);
        int count = 20;
        List<AlertNotificationModel> allModels = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            String notificationJson = TestResourceUtils.readFileToString("json/projectVersionNotification.json");
            AlertNotificationModel model = new AlertNotificationModel(
                Integer.valueOf(index).longValue(), 1L,
                "BlackDuck",
                "BlackDuck_1",
                "PROJECT_VERSION",
                notificationJson,
                DateUtils.createCurrentDateTimestamp(),
                DateUtils.createCurrentDateTimestamp(),
                false,
                String.format("content-id-%s", UUID.randomUUID())
            );
            allModels.add(model);
        }
        notificationManager.saveAllNotifications(allModels);
        DateRange dateRange = task.getDateRange();
        int pageSize = 5;
        int totalPages = count / pageSize;

        List<AlertNotificationModel> testModels = new ArrayList<>(count);
        for (int currentPage = 0; currentPage < totalPages; currentPage++) {
            AlertPagedModel<AlertNotificationModel> pagedModel = task.read(dateRange, currentPage, pageSize);
            assertEquals(totalPages, pagedModel.getTotalPages());
            assertEquals(currentPage, pagedModel.getCurrentPage());
            assertEquals(pageSize, pagedModel.getPageSize());
            testModels.addAll(pagedModel.getModels());
        }
        assertEquals(allModels.size(), testModels.size());
        assertTrue(allModels.containsAll(testModels));
    }

    private Answer<AlertPagedModel<AlertNotificationModel>> createPagedQuery(List<AlertNotificationModel> allModels) {
        return (invocation) -> {
            OffsetDateTime startTime = invocation.getArgument(0);
            OffsetDateTime endTime = invocation.getArgument(1);
            int pageNumber = invocation.getArgument(2);
            int pageSize = invocation.getArgument(3);
            int startIndex = pageSize * pageNumber;
            int endIndex = startIndex + pageSize;
            int totalPages = allModels.size() / pageSize;
            List<AlertNotificationModel> modelsInPage = new LinkedList<>();
            if (startIndex < allModels.size()) {
                for (int index = startIndex; index < endIndex; index++) {
                    AlertNotificationModel model = allModels.get(index);
                    if (model.getCreatedAt().isBefore(endTime) && model.getCreatedAt().isAfter(startTime)) {
                        modelsInPage.add(model);
                    }
                }
            }
            return new AlertPagedModel<>(totalPages, pageNumber, pageSize, modelsInPage);
        };
    }

    private void assertDateIsEqual(OffsetDateTime expected, ZonedDateTime actual) {
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }

    private ProcessingTask createTask(
        TaskScheduler taskScheduler,
        NotificationAccessor notificationManager,
        NotificationMappingProcessor notificationMappingProcessor,
        TaskManager taskManager,
        JobAccessor jobAccessor
    ) {
        return new ProcessingTask(taskScheduler, taskManager, notificationManager, notificationMappingProcessor, jobAccessor, FrequencyType.DAILY, createEventManager()) {
            @Override
            public String scheduleCronExpression() {
                return null;
            }

            @Override
            public DateRange getDateRange() {
                return DateRange.of(getLastRunTime(), DateUtils.createCurrentDateTimestamp());
            }
        };
    }

    private EventManager createEventManager() {
        return Mockito.mock(EventManager.class);
    }
}

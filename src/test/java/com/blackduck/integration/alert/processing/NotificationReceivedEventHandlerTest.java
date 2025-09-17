/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.event.JobNotificationMappedEvent;
import com.blackduck.integration.alert.api.processor.mapping.JobNotificationMapper2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;

import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;
import com.blackduck.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.alert.test.common.TestResourceUtils;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

class NotificationReceivedEventHandlerTest {

    private final MockAlertProperties alertProperties = new MockAlertProperties();

    @Test
    void handleEventTest() throws IOException {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(1L, false, false);
        List<AlertNotificationModel> alertNotificationModels = List.of(alertNotificationModel);
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        NotificationMappingProcessor notificationMappingProcessor = Mockito.mock(NotificationMappingProcessor.class);
        EventManager eventManager = mockEventManager();
        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationMappingProcessor, eventManager);

        try {
            eventHandler.handle(new NotificationReceivedEvent(2L));
        } catch (RuntimeException e) {
            fail("Unable to handle event", e);
        }
    }

    @Test
    void notificationsTableEmptyTest() {
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(List.of());
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = Mockito.mock(NotificationDetailExtractionDelegator.class);
        JobNotificationMapper2 jobNotificationMapper = Mockito.mock(JobNotificationMapper2.class);

        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(notificationDetailExtractionDelegator, jobNotificationMapper, notificationAccessor, alertProperties);
        RecordingEventManager eventManager = mockEventManager();

        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationMappingProcessor, eventManager);
        eventHandler.handle(new NotificationReceivedEvent(2L));

        Assertions.assertTrue(eventManager.getEventList().isEmpty());
    }

    @Test
    void allNotificationsMappedTest() throws Exception {
        Long notificationId = 1L;
        int count = 10;
        List<AlertNotificationModel> alertNotificationModels = new ArrayList<>(count);
        AtomicLong counter = new AtomicLong(notificationId);
        for(int index = 0; index < count; index++) {
            alertNotificationModels.add(createAlertNotificationModel(counter.getAndIncrement(), false, false));
        }
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = Mockito.mock(NotificationDetailExtractionDelegator.class);
        JobNotificationMapper2 jobNotificationMapper = Mockito.mock(JobNotificationMapper2.class);
        // the batch limit has been exceeded
        Mockito.when(jobNotificationMapper.hasBatchReachedSizeLimit(Mockito.any(UUID.class), Mockito.anyInt())).thenReturn(true);

        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(notificationDetailExtractionDelegator, jobNotificationMapper, notificationAccessor, alertProperties);
        RecordingEventManager eventManager = mockEventManager();

        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationMappingProcessor, eventManager);
        eventHandler.handle(new NotificationReceivedEvent(2L));

        List<Long> notificationIds = alertNotificationModels.stream()
                                .map(AlertNotificationModel::getId)
                                .toList();
        List<AlertNotificationModel> updatedNotificationModels = notificationAccessor.findByIds(notificationIds);
        boolean allNotificationsMapping = updatedNotificationModels.stream()
                .allMatch(AlertNotificationModel::isMappingToJobs);

        Assertions.assertTrue(allNotificationsMapping, "Expected all notifications to be marked as mapping to jobs.");
        // Send an event to continue processing notifications that have not been mapped
        Assertions.assertEquals(1, eventManager.getEventList().size());
        Assertions.assertEquals(JobNotificationMappedEvent.NOTIFICATION_MAPPED_EVENT_TYPE, eventManager.getEventList().get(0).getDestination());
    }

    @Test
    void allNotificationsMappedForASinglePageTest() throws Exception {
        Long notificationId = 1L;
        int count = NotificationReceivedEventHandler.PAGE_SIZE + 10;
        List<AlertNotificationModel> alertNotificationModels = new ArrayList<>(count);
        AtomicLong counter = new AtomicLong(notificationId);
        for(int index = 0; index < count; index++) {
            alertNotificationModels.add(createAlertNotificationModel(counter.getAndIncrement(), false, false));
        }
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = Mockito.mock(NotificationDetailExtractionDelegator.class);
        JobNotificationMapper2 jobNotificationMapper = Mockito.mock(JobNotificationMapper2.class);

        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(notificationDetailExtractionDelegator, jobNotificationMapper, notificationAccessor, alertProperties);
        RecordingEventManager eventManager = mockEventManager();

        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationMappingProcessor, eventManager);
        eventHandler.handle(new NotificationReceivedEvent(2L));

        List<Long> notificationIds = alertNotificationModels.stream()
                .map(AlertNotificationModel::getId)
                .toList();
        List<AlertNotificationModel> updatedNotificationModels = notificationAccessor.findByIds(notificationIds);
        boolean allNotificationsMapping = updatedNotificationModels.stream()
                .limit(NotificationReceivedEventHandler.PAGE_SIZE)
                .allMatch(AlertNotificationModel::isMappingToJobs);

        Assertions.assertTrue(allNotificationsMapping, "Expected all notifications to be marked as mapping to jobs.");
        // Send an event to continue processing notifications that have not been mapped
        Assertions.assertEquals(1, eventManager.getEventList().size());
        Assertions.assertEquals(NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE, eventManager.getEventList().get(0).getDestination());
    }

    @Test
    void batchLimitExceededTest() throws Exception {
        Long notificationId = 1L;
        int count = NotificationReceivedEventHandler.PAGE_SIZE + 10;
        List<AlertNotificationModel> alertNotificationModels = new ArrayList<>(count);
        AtomicLong counter = new AtomicLong(notificationId);
        for(int index = 0; index < count; index++) {
            alertNotificationModels.add(createAlertNotificationModel(counter.getAndIncrement(), false, false));
        }
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = Mockito.mock(NotificationDetailExtractionDelegator.class);
        JobNotificationMapper2 jobNotificationMapper = Mockito.mock(JobNotificationMapper2.class);
        // the batch limit has been exceeded
        Mockito.when(jobNotificationMapper.hasBatchReachedSizeLimit(Mockito.any(UUID.class), Mockito.anyInt())).thenReturn(true);

        NotificationMappingProcessor notificationMappingProcessor = new NotificationMappingProcessor(notificationDetailExtractionDelegator, jobNotificationMapper, notificationAccessor, alertProperties);
        RecordingEventManager eventManager = mockEventManager();

        NotificationReceivedEventHandler eventHandler = new NotificationReceivedEventHandler(notificationAccessor, notificationMappingProcessor, eventManager);
        eventHandler.handle(new NotificationReceivedEvent(2L));
        AlertNotificationModel updatedNotificationModel = notificationAccessor.findById(notificationId).orElseThrow(() -> new AssertionError("Expected notification not found.  Expected to find a notification with id: " + notificationId));

        Assertions.assertTrue(updatedNotificationModel.isMappingToJobs());
        Assertions.assertTrue(updatedNotificationModel.getProcessed());
        // expect 2 events to be sent when the batch limit is reached.
        // First is to send the event to indicate notifications have been mapped to jobs
        // Second is to send an event to continue processing notifications that have not been mapped
        Assertions.assertEquals(2, eventManager.getEventList().size(), "Expected to find 2 events in the event manager.");
        Assertions.assertEquals(JobNotificationMappedEvent.NOTIFICATION_MAPPED_EVENT_TYPE, eventManager.getEventList().get(0).getDestination());
        Assertions.assertEquals(NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE, eventManager.getEventList().get(1).getDestination());
    }

    private AlertNotificationModel createAlertNotificationModel(Long id, boolean processed,boolean mappingToJobs) throws IOException {
        Long providerConfigId = 2L;
        String provider = "provider-test";
        String notificationType = "PROJECT_VERSION";
        String content = TestResourceUtils.readFileToString("json/projectVersionNotification.json");
        String providerConfigName = "providerConfigName";
        String contentId = String.format("content-id-%s", UUID.randomUUID());

        return new AlertNotificationModel(
            id,
            providerConfigId,
            provider,
            providerConfigName,
            notificationType,
            content,
            DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(),
            processed,
            contentId,
            mappingToJobs
        );
    }

    private RecordingEventManager mockEventManager() {
        RabbitTemplate rabbitTemplate = Mockito.mock(RabbitTemplate.class);
        Mockito.doNothing().when(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.any(Object.class));
        Gson gson = BlackDuckServicesFactory.createDefaultGson();

        return new RecordingEventManager(gson, rabbitTemplate, new SyncTaskExecutor());
    }

}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;
import com.blackduck.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.api.processor.detail.NotificationDetailExtractionDelegator;
import com.blackduck.integration.alert.api.processor.mapping.JobNotificationMapper2;
import com.blackduck.integration.alert.api.provider.ProviderDescriptor;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.job.api.DefaultConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.database.job.api.DefaultNotificationAccessor;
import com.blackduck.integration.alert.database.notification.NotificationEntity;
import com.blackduck.integration.alert.mock.entity.MockNotificationContent;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.blackduck.integration.alert.test.common.TestProperties;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;

//TODO: This class depends on AlertIntegrationTest which cannot be moved into test-common yet due to it's dependencies.
//  Move this class into the workflow subproject once the dependencies are resolved
@AlertIntegrationTest
class NotificationReceivedEventHandlerTestIT {

    @Autowired
    private DefaultNotificationAccessor defaultNotificationAccessor;
    @Autowired
    private DefaultConfigurationModelConfigurationAccessor defaultConfigurationAccessor;
    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;
    @Autowired
    private EventManager eventManager;
    @Autowired
    private JobNotificationMapper2 jobNotificationMapper2;
    @Autowired
    private AlertProperties alertProperties;

    private Long blackDuckGlobalConfigId;
    private TestProperties properties;

    int pageSize = 10;

    @BeforeEach
    public void init() {
        properties = new TestProperties();
        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("TRUE");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue("blackduck-config");
        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue(properties.getBlackDuckURL());
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.createSensitive(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue(properties.getBlackDuckAPIToken());
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue(String.valueOf(BlackDuckProperties.DEFAULT_TIMEOUT));

        ConfigurationModel blackduckConfigurationModel = defaultConfigurationAccessor.createConfiguration(blackDuckProviderKey,
            ConfigContextEnum.GLOBAL,
            List.of(providerConfigEnabled,
                providerConfigName,
                blackduckUrl,
                blackduckApiKey,
                blackduckTimeout));
        blackDuckGlobalConfigId = blackduckConfigurationModel.getConfigurationId();
    }

    @AfterEach
    public void cleanUpDB() {
        PageRequest pageRequest = defaultNotificationAccessor.getPageRequestForNotifications(0, pageSize, null, null);
        Page<AlertNotificationModel> notifications = defaultNotificationAccessor.findAll(pageRequest, false);
        notifications.get().forEach(defaultNotificationAccessor::deleteNotification);
        defaultNotificationAccessor.findByCreatedAtBefore(OffsetDateTime.now().minusMinutes(2));
        defaultConfigurationAccessor.deleteConfiguration(blackDuckGlobalConfigId);
    }

    @Test
    void testHandleEventNotProcessedNotifications() {

        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        notificationContent.add(createAlertNotificationModel(false));
        notificationContent.add(createAlertNotificationModel(false));

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);

        NotificationMappingProcessor notificationMappingProcessor = createNotificationMappingProcessor();
        NotificationReceivedEventHandler notificationReceivedEventHandler = new NotificationReceivedEventHandler(
            defaultNotificationAccessor,
            notificationMappingProcessor,
            eventManager
        );
        notificationReceivedEventHandler.handle(new NotificationReceivedEvent(blackDuckGlobalConfigId));

        testAlertNotificationModels(savedModels);
    }

    @Test
    void testHandleEventProcessedNotifications() {
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        notificationContent.add(createAlertNotificationModel(true));
        notificationContent.add(createAlertNotificationModel(true));

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);
        assertEquals(0, defaultNotificationAccessor.getFirstPageOfNotificationsNotProcessed(pageSize).getModels().size());

        NotificationMappingProcessor notificationMappingProcessor = createNotificationMappingProcessor();
        NotificationReceivedEventHandler notificationReceivedEventHandler = new NotificationReceivedEventHandler(
            defaultNotificationAccessor,
            notificationMappingProcessor,
            eventManager
        );
        notificationReceivedEventHandler.handle(new NotificationReceivedEvent(blackDuckGlobalConfigId));

        testAlertNotificationModels(savedModels);
    }

    @Test
    void testHandleEventMixedProcessedNotifications() {
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        notificationContent.add(createAlertNotificationModel(true));
        notificationContent.add(createAlertNotificationModel(false));

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);

        NotificationMappingProcessor notificationMappingProcessor = createNotificationMappingProcessor();
        NotificationReceivedEventHandler notificationReceivedEventHandler = new NotificationReceivedEventHandler(
            defaultNotificationAccessor,
            notificationMappingProcessor,
            eventManager
        );
        notificationReceivedEventHandler.handle(new NotificationReceivedEvent(blackDuckGlobalConfigId));

        testAlertNotificationModels(savedModels);
    }

    @Test
    void testHandleEventProcessedNotificationsWithPages() {
        EventManager eventManagerSpy = Mockito.spy(eventManager);
        int totalNotifications = 400;
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        for (int index = 0; index < totalNotifications; index++) {
            notificationContent.add(createAlertNotificationModel(false));
        }
        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);

        NotificationMappingProcessor notificationMappingProcessor = createNotificationMappingProcessor();
        NotificationReceivedEventHandler notificationReceivedEventHandler = new NotificationReceivedEventHandler(
            defaultNotificationAccessor,
            notificationMappingProcessor,
            eventManagerSpy
        );
        notificationReceivedEventHandler.handle(new NotificationReceivedEvent(blackDuckGlobalConfigId));

        assertEquals(200, defaultNotificationAccessor.getFirstPageOfNotificationsNotProcessed(200).getModels().size());
    }

    private AlertNotificationModel createAlertNotificationModel(boolean processed) {
        String bomEditContent = "{"
            + "\"type\":\"" + NotificationType.BOM_EDIT.name() + "\","
            + "\"content\": {"
            + "\"projectVersion\": \"" + properties.getBlackDuckURL() + "/api/projects\","
            + "\"bomComponent\": \"" + properties.getBlackDuckURL() + "\","
            + "\"componentName\": \"test\","
            + "\"componentVersionName\": \"test\""
            + "}"
            + "}";
        MockNotificationContent notificationMocker = new MockNotificationContent(DateUtils.createCurrentDateTimestamp(), blackDuckProviderKey.getUniversalKey(), DateUtils.createCurrentDateTimestamp(), NotificationType.BOM_EDIT.name(),
            bomEditContent, null, blackDuckGlobalConfigId);
        NotificationEntity entity = notificationMocker.createEntity();
        return new AlertNotificationModel(
            null,
            entity.getProviderConfigId(),
            entity.getProvider(),
            "providerConfigName",
            entity.getNotificationType(),
            entity.getContent(),
            entity.getCreatedAt(),
            entity.getProviderCreationTime(),
            processed,
            String.format("content-id-%s", UUID.randomUUID())
        );
    }

    private void testAlertNotificationModels(List<AlertNotificationModel> models) {
        PageRequest pageRequest = defaultNotificationAccessor.getPageRequestForNotifications(0, models.size(), null, null);
        List<AlertNotificationModel> alertNotificationModels = defaultNotificationAccessor.findAll(pageRequest, false).getContent();

        assertModelsAreProcessed(alertNotificationModels);
        assertEquals(0, defaultNotificationAccessor.getFirstPageOfNotificationsNotProcessed(pageSize).getModels().size());
    }

    private void assertModelsAreProcessed(List<AlertNotificationModel> notificationModels) {
        for (AlertNotificationModel notificationModel : notificationModels) {
            assertTrue(notificationModel.getProcessed());
        }
    }

    private NotificationMappingProcessor createNotificationMappingProcessor() {
        // We aren't testing the processor here since we have bad data.  We just want to make sure the processor marks the notifications as processed to test the paging in the handler.
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = Mockito.mock(NotificationDetailExtractionDelegator.class);
        Mockito.when(notificationDetailExtractionDelegator.wrapNotification(Mockito.any())).thenReturn(List.of());
        return new NotificationMappingProcessor(
            notificationDetailExtractionDelegator,
            jobNotificationMapper2,
            defaultNotificationAccessor,
            alertProperties
        );
    }

}

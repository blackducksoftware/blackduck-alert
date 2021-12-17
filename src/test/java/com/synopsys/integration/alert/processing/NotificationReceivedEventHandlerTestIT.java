package com.synopsys.integration.alert.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.synopsys.integration.alert.api.event.EventManager;
import com.synopsys.integration.alert.api.event.NotificationReceivedEvent;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.processor.api.NotificationContentProcessor;
import com.synopsys.integration.alert.processor.api.NotificationProcessingLifecycleCache;
import com.synopsys.integration.alert.processor.api.NotificationProcessor;
import com.synopsys.integration.alert.processor.api.detail.NotificationDetailExtractionDelegator;
import com.synopsys.integration.alert.processor.api.distribute.ProviderMessageDistributor;
import com.synopsys.integration.alert.processor.api.filter.JobNotificationMapper;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.test.common.TestProperties;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;

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
    private JobNotificationMapper jobNotificationMapper;
    @Autowired
    private NotificationContentProcessor notificationContentProcessor;
    @Autowired
    private ProviderMessageDistributor providerMessageDistributor;
    @Autowired
    private List<NotificationProcessingLifecycleCache> lifecycleCaches;
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

        NotificationProcessor notificationProcessor = createNotificationProcessor();
        NotificationReceivedEventHandler notificationReceivedEventHandler = new NotificationReceivedEventHandler(defaultNotificationAccessor, notificationProcessor, eventManager);
        notificationReceivedEventHandler.handle(new NotificationReceivedEvent());

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

        NotificationProcessor notificationProcessor = createNotificationProcessor();
        NotificationReceivedEventHandler notificationReceivedEventHandler = new NotificationReceivedEventHandler(defaultNotificationAccessor, notificationProcessor, eventManager);
        notificationReceivedEventHandler.handle(new NotificationReceivedEvent());

        testAlertNotificationModels(savedModels);
    }

    @Test
    void testHandleEventMixedProcessedNotifications() {
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        notificationContent.add(createAlertNotificationModel(true));
        notificationContent.add(createAlertNotificationModel(false));

        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);

        NotificationProcessor notificationProcessor = createNotificationProcessor();
        NotificationReceivedEventHandler notificationReceivedEventHandler = new NotificationReceivedEventHandler(defaultNotificationAccessor, notificationProcessor, eventManager);
        notificationReceivedEventHandler.handle(new NotificationReceivedEvent());

        testAlertNotificationModels(savedModels);
    }

    @Test
    void testHandleEventProcessedNotificationsWithPages() {
        EventManager eventManagerSpy = Mockito.spy(eventManager);
        int totalNotifications = 200;
        List<AlertNotificationModel> notificationContent = new ArrayList<>();
        for (int index = 0; index < totalNotifications; index++) {
            notificationContent.add(createAlertNotificationModel(false));
        }
        List<AlertNotificationModel> savedModels = defaultNotificationAccessor.saveAllNotifications(notificationContent);
        assertNotNull(savedModels);

        NotificationProcessor notificationProcessor = createNotificationProcessor();
        NotificationReceivedEventHandler notificationReceivedEventHandler = new NotificationReceivedEventHandler(defaultNotificationAccessor, notificationProcessor, eventManagerSpy);
        notificationReceivedEventHandler.handle(new NotificationReceivedEvent());

        Mockito.verify(eventManagerSpy, Mockito.atLeastOnce()).sendEvent(Mockito.any());
        assertEquals(100, defaultNotificationAccessor.getFirstPageOfNotificationsNotProcessed(100).getModels().size());
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
            processed
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

    private NotificationProcessor createNotificationProcessor() {
        // We aren't testing the processor here since we have bad data.  We just want to make sure the processor marks the notifications as processed to test the paging in the handler.
        NotificationDetailExtractionDelegator notificationDetailExtractionDelegator = Mockito.mock(NotificationDetailExtractionDelegator.class);
        Mockito.when(notificationDetailExtractionDelegator.wrapNotification(Mockito.any())).thenReturn(List.of());
        return new NotificationProcessor(notificationDetailExtractionDelegator,
            jobNotificationMapper,
            notificationContentProcessor,
            providerMessageDistributor,
            lifecycleCaches,
            defaultNotificationAccessor
        );
    }

}

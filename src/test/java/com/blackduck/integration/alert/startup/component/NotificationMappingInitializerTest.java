package com.blackduck.integration.alert.startup.component;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.event.AlertEvent;
import com.blackduck.integration.alert.api.event.NotificationReceivedEvent;
import com.blackduck.integration.alert.api.provider.Provider;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.blackduck.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.blackduck.integration.alert.common.persistence.model.ConfigurationModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.processing.MockNotificationAccessor;
import com.blackduck.integration.alert.processing.RecordingEventManager;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProvider;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

class NotificationMappingInitializerTest {

    @Test
    void emptyProvidersListTest() {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(1L, 1L, false, false);
        List<AlertNotificationModel> alertNotificationModels = List.of(alertNotificationModel);
        List<Provider> providers = List.of();
        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        RecordingEventManager eventManager = mockEventManager();

        NotificationMappingInitializer mappingInitializer = new NotificationMappingInitializer(providers, null,notificationAccessor, eventManager);
        mappingInitializer.initialize();

        Assertions.assertTrue(eventManager.getEventList().isEmpty());
    }

    @Test
    void providerConfigurationEmptyTest() {
        AlertNotificationModel alertNotificationModel = createAlertNotificationModel(1L, 1L,false, false);
        List<AlertNotificationModel> alertNotificationModels = List.of(alertNotificationModel);
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        Provider provider = new BlackDuckProvider(blackDuckProviderKey, null,null,null);
        List<Provider> providers = List.of(provider);
        ConfigurationModelConfigurationAccessor providerConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(providerConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(blackDuckProviderKey, ConfigContextEnum.GLOBAL)).thenReturn(List.of());

        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        RecordingEventManager eventManager = mockEventManager();

        NotificationMappingInitializer mappingInitializer = new NotificationMappingInitializer(providers, providerConfigurationAccessor, notificationAccessor, eventManager);
        mappingInitializer.initialize();

        Assertions.assertTrue(eventManager.getEventList().isEmpty());
    }

    @Test
    void noNotificationsToMapTest() {
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        Provider provider = new BlackDuckProvider(blackDuckProviderKey, null,null,null);
        List<Provider> providers = List.of(provider);

        ConfigurationModelConfigurationAccessor providerConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        int count = 5;
        AtomicLong configIdCounter = new AtomicLong(1L);
        List<ConfigurationModel> providerConfigurations = new ArrayList<>(count);
        List<AlertNotificationModel> alertNotificationModels = new  ArrayList<>(count);
        for(int index = 0; index < count; index++) {
            Long providerConfigId = configIdCounter.getAndIncrement();
            ConfigurationModel configurationModel = new ConfigurationModel(0L, providerConfigId, "", "", ConfigContextEnum.GLOBAL);
            providerConfigurations.add(configurationModel);
            AlertNotificationModel alertNotificationModel = createAlertNotificationModel(providerConfigId, providerConfigId, true, true);
            alertNotificationModels.add(alertNotificationModel);
        }
        Mockito.when(providerConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(blackDuckProviderKey, ConfigContextEnum.GLOBAL)).thenReturn(providerConfigurations);

        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        RecordingEventManager eventManager = mockEventManager();

        NotificationMappingInitializer mappingInitializer = new NotificationMappingInitializer(providers, providerConfigurationAccessor, notificationAccessor, eventManager);
        mappingInitializer.initialize();

        Assertions.assertTrue(eventManager.getEventList().isEmpty());
    }

    @Test
    void mappingRestartedTest() {
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        Provider provider = new BlackDuckProvider(blackDuckProviderKey, null,null,null);
        List<Provider> providers = List.of(provider);

        ConfigurationModelConfigurationAccessor providerConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        int count = 5;
        AtomicLong configIdCounter = new AtomicLong(1L);
        List<ConfigurationModel> providerConfigurations = new ArrayList<>(count);
        List<AlertNotificationModel> alertNotificationModels = new  ArrayList<>(count);
        for(int index = 0; index < count; index++) {
            Long providerConfigId = configIdCounter.getAndIncrement();
            ConfigurationModel configurationModel = new ConfigurationModel(0L, providerConfigId, "", "", ConfigContextEnum.GLOBAL);
            providerConfigurations.add(configurationModel);
            AlertNotificationModel alertNotificationModel = createAlertNotificationModel(providerConfigId, providerConfigId, false, true);
            alertNotificationModels.add(alertNotificationModel);
        }
        Mockito.when(providerConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(blackDuckProviderKey, ConfigContextEnum.GLOBAL)).thenReturn(providerConfigurations);

        NotificationAccessor notificationAccessor = new MockNotificationAccessor(alertNotificationModels);
        RecordingEventManager eventManager = mockEventManager();

        NotificationMappingInitializer mappingInitializer = new NotificationMappingInitializer(providers, providerConfigurationAccessor, notificationAccessor, eventManager);
        mappingInitializer.initialize();
        boolean allNotificationReceivedEvents = eventManager.getEventList().stream()
                .map(AlertEvent::getDestination)
                .allMatch(NotificationReceivedEvent.NOTIFICATION_RECEIVED_EVENT_TYPE::equals);
        // Send an event to continue processing notifications that have not been mapped
        Assertions.assertEquals(count, eventManager.getEventList().size());
        Assertions.assertTrue(allNotificationReceivedEvents);
    }

    private AlertNotificationModel createAlertNotificationModel(Long id, Long providerConfigId, boolean processed, boolean mappingToJobs) {
        String provider = "provider-test";
        String notificationType = "PROJECT_VERSION";
        String content = "Notification content";
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

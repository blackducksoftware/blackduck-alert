package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;

class DefaultNotificationAccessorTest {
    private final Long id = 1L;
    private final Long providerConfigId = 2L;

    private final String provider = "provider-test";
    private final String providerConfigName = "providerConfigName-test";
    private final String notificationType = "notificationType-test";
    private final String content = "content";

    private final String KEY_PROVIDER_CONFIG_NAME = ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME;
    private final String fieldValue = "test-channel.common.name-value";

    private final AlertNotificationModel expectedAlertNotificationModel = new AlertNotificationModel(id, providerConfigId, provider, fieldValue, notificationType, content, DateUtils.createCurrentDateTimestamp(),
        DateUtils.createCurrentDateTimestamp(), false);

    @Test
    void saveAllNotificationsTest() {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = createdAt.minusSeconds(10);

        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(null, providerConfigId, provider, providerConfigName, notificationType, content, createdAt, providerCreationTime, false);
        NotificationEntity notificationEntity = new NotificationEntity(id, createdAt, provider, providerConfigId, providerCreationTime, notificationType, content, false);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.saveAll(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.saveAllNotifications(List.of(alertNotificationModel));

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel testAlertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, testAlertNotificationModel);
    }

    @Test
    void saveAllNotificationsEmptyModelListTest() {
        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);

        Mockito.when(notificationContentRepository.saveAll(Mockito.any())).thenReturn(new ArrayList<>());

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, null);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.saveAllNotifications(new ArrayList<>());

        assertTrue(alertNotificationModelList.isEmpty());
    }

    @Test
    void finalAllTest() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        Page<NotificationEntity> allSentNotifications = new PageImpl<>(List.of(notificationEntity));
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findAllSentNotifications(Mockito.any())).thenReturn(allSentNotifications);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        Page<AlertNotificationModel> alertNotificationModelPage = notificationManager.findAll(pageRequest, Boolean.TRUE);

        assertEquals(1, alertNotificationModelPage.getTotalPages());
        AlertNotificationModel testAlertNotificationModel = alertNotificationModelPage.getContent().get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, testAlertNotificationModel);
    }

    @Test
    void finalAllShowNotificationsFalseTest() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        Page<NotificationEntity> allSentNotifications = new PageImpl<>(List.of(notificationEntity));
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));
        Mockito.when(notificationContentRepository.findAll(pageRequest)).thenReturn(allSentNotifications);

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        Page<AlertNotificationModel> alertNotificationModelPage = notificationManager.findAll(pageRequest, Boolean.FALSE);

        assertEquals(1, alertNotificationModelPage.getTotalPages());
        AlertNotificationModel testAlertNotificationModel = alertNotificationModelPage.getContent().get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, testAlertNotificationModel);
    }

    @Test
    void findAllWithSearchTest() {
        final String searchTerm = "searchTerm-test";
        PageRequest pageRequest = PageRequest.of(0, 10);

        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        Page<NotificationEntity> notificationEntityPage = new PageImpl<>(List.of(notificationEntity));
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findMatchingSentNotification(Mockito.any(), Mockito.any())).thenReturn(notificationEntityPage);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));
        Mockito.when(notificationContentRepository.findMatchingNotification(Mockito.any(), Mockito.any())).thenReturn(notificationEntityPage);

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        Page<AlertNotificationModel> alertNotificationModelPage = notificationManager.findAllWithSearch(searchTerm, pageRequest, Boolean.TRUE);
        Page<AlertNotificationModel> alertNotificationModelPageShowNotificationsFalse = notificationManager.findAllWithSearch(searchTerm, pageRequest, Boolean.FALSE);

        assertEquals(1, alertNotificationModelPage.getTotalPages());
        AlertNotificationModel testAlertNotificationModel = alertNotificationModelPage.getContent().get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, testAlertNotificationModel);

        assertEquals(1, alertNotificationModelPageShowNotificationsFalse.getTotalPages());
        AlertNotificationModel testAlertNotificationModel2 = alertNotificationModelPageShowNotificationsFalse.getContent().get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, testAlertNotificationModel2);
    }

    @Test
    void findByIdsTest() {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findAllById(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.findByIds(List.of(1L));

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel alertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel);
    }

    @Test
    void findByIdTest() {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findById(Mockito.any())).thenReturn(Optional.of(notificationEntity));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        Optional<AlertNotificationModel> alertNotificationModel = notificationManager.findById(1L);

        assertTrue(alertNotificationModel.isPresent());
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel.get());
    }

    @Test
    void findByCreatedAtBetweenTest() {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findByCreatedAtBetween(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new PageImpl<>(List.of(notificationEntity)));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.findByCreatedAtBetween(DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(),
                AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE)
            .getModels();

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel alertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel);
    }

    @Test
    void findByCreatedAtBeforeTest() {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findByCreatedAtBefore(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.findByCreatedAtBefore(DateUtils.createCurrentDateTimestamp());

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel alertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel);
    }

    @Test
    void findByCreatedAtBeforeDayOffsetTest() {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findByCreatedAtBefore(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.findByCreatedAtBeforeDayOffset(1);

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel alertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel);
    }

    @Test
    void getPageRequestForNotificationsTest() {
        final int pageNumber = 1;
        final int pageSize = 1;
        final String sortField = "content";
        String sortOrder = Sort.Direction.ASC.name();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, auditEntryRepository, configurationModelConfigurationAccessor);
        PageRequest pageRequest = notificationManager.getPageRequestForNotifications(pageNumber, pageSize, sortField, sortOrder);

        assertEquals(pageNumber, pageRequest.getPageNumber());
        assertEquals(pageSize, pageRequest.getPageSize());
    }

    @Test
    void getFirstPageOfNotificationsNotProcessedTest() {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        Page<NotificationEntity> pageOfNotificationEntities = new PageImpl<>(List.of(notificationEntity));
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findByProcessedFalseOrderByProviderCreationTimeAsc(Mockito.any())).thenReturn(pageOfNotificationEntities);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        AlertPagedModel<AlertNotificationModel> model = notificationManager.getFirstPageOfNotificationsNotProcessed(100);

        List<AlertNotificationModel> alertNotificationModelList = model.getModels();
        assertEquals(1, alertNotificationModelList.size());
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModelList.get(0));
    }

    @Test
    void setNotificationsProcessedTest() {
        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(null, providerConfigId, provider, providerConfigName, notificationType, content, DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), false);

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        notificationManager.setNotificationsProcessed(List.of(alertNotificationModel));

        Mockito.verify(notificationContentRepository).setProcessedByIds(Mockito.any());
    }

    @Test
    void setNotificationsProcessedByIdTest() {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content, false);
        ConfigurationModel configurationModel = createConfigurationModel();
        Set<Long> notificationIds = Set.of(1L);

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findAllById(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, configurationModelConfigurationAccessor);
        notificationManager.setNotificationsProcessedById(notificationIds);

        Mockito.verify(notificationContentRepository).setProcessedByIds(Mockito.any());
    }

    @Test
    void hasMoreNotificationsToProcessFalseTest() {
        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        Mockito.when(notificationContentRepository.existsByProcessedFalse()).thenReturn(Boolean.FALSE);
        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, null);
        assertFalse(notificationManager.hasMoreNotificationsToProcess());
    }

    @Test
    void hasMoreNotificationsToProcessTrueTest() {
        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        Mockito.when(notificationContentRepository.existsByProcessedFalse()).thenReturn(Boolean.TRUE);
        DefaultNotificationAccessor notificationManager = new DefaultNotificationAccessor(notificationContentRepository, null, null);
        assertTrue(notificationManager.hasMoreNotificationsToProcess());
    }

    private void testExpectedAlertNotificationModel(AlertNotificationModel expected, AlertNotificationModel actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getProviderConfigId(), actual.getProviderConfigId());
        assertEquals(expected.getProvider(), actual.getProvider());
        assertEquals(expected.getProviderConfigName(), actual.getProviderConfigName());
        assertEquals(expected.getNotificationType(), actual.getNotificationType());
        assertEquals(expected.getContent(), actual.getContent());
    }

    private ConfigurationModelMutable createConfigurationModel() {
        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(1L, 1L, "createdAt-test", "lastUpdate-test", ConfigContextEnum.DISTRIBUTION);
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(fieldValue);
        configurationModel.put(configurationFieldModel);

        return configurationModel;
    }

}

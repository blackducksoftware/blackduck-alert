package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.event.EventManager;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;

public class DefaultNotificationManagerTest {

    private final Long id = 1L;
    private final Long providerConfigId = 2L;

    private final String provider = "provider-test";
    private final String providerConfigName = "providerConfigName-test";
    private final String notificationType = "notificationType-test";
    private final String content = "content";

    private final String KEY_PROVIDER_CONFIG_NAME = ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME;
    private final String fieldValue = "test-channel.common.name-value";

    private AlertNotificationModel expectedAlertNotificationModel = new AlertNotificationModel(id, providerConfigId, provider, fieldValue, notificationType, content, DateUtils.createCurrentDateTimestamp(),
        DateUtils.createCurrentDateTimestamp());

    @Test
    public void saveAllNotificationsTest() throws Exception {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = createdAt.minusSeconds(10);

        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(null, providerConfigId, provider, providerConfigName, notificationType, content, createdAt, providerCreationTime);
        NotificationEntity notificationEntity = new NotificationEntity(id, createdAt, provider, providerConfigId, providerCreationTime, notificationType, content);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        EventManager eventManager = Mockito.mock(EventManager.class);

        Mockito.when(notificationContentRepository.saveAll(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, eventManager);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.saveAllNotifications(List.of(alertNotificationModel));

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel testAlertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, testAlertNotificationModel);
    }

    @Test
    public void saveAllNotificationsEmptyModelListTest() {
        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);

        Mockito.when(notificationContentRepository.saveAll(Mockito.any())).thenReturn(new ArrayList<>());

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, null, null);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.saveAllNotifications(new ArrayList<>());

        assertTrue(alertNotificationModelList.isEmpty());
    }

    @Test
    public void finalAllTest() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10);

        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content);
        Page<NotificationEntity> allSentNotifications = new PageImpl<>(List.of(notificationEntity));
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findAllSentNotifications(Mockito.any())).thenReturn(allSentNotifications);
        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, null);
        Page<AlertNotificationModel> alertNotificationModelPage = notificationManager.findAll(pageRequest, Boolean.TRUE);

        assertEquals(1, alertNotificationModelPage.getTotalPages());
        AlertNotificationModel testAlertNotificationModel = alertNotificationModelPage.getContent().get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, testAlertNotificationModel);
    }

    @Test
    public void finalAllShowNotificationsFalseTest() throws Exception {
        PageRequest pageRequest = PageRequest.of(0, 10);

        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content);
        Page<NotificationEntity> allSentNotifications = new PageImpl<>(List.of(notificationEntity));
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));
        Mockito.when(notificationContentRepository.findAll(pageRequest)).thenReturn(allSentNotifications);

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, null);
        Page<AlertNotificationModel> alertNotificationModelPage = notificationManager.findAll(pageRequest, Boolean.FALSE);

        assertEquals(1, alertNotificationModelPage.getTotalPages());
        AlertNotificationModel testAlertNotificationModel = alertNotificationModelPage.getContent().get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, testAlertNotificationModel);
    }

    @Test
    public void findAllWithSearchTest() throws Exception {
        final String searchTerm = "searchTerm-test";
        PageRequest pageRequest = PageRequest.of(0, 10);

        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content);
        Page<NotificationEntity> notificationEntityPage = new PageImpl<>(List.of(notificationEntity));
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findMatchingSentNotification(Mockito.any(), Mockito.any())).thenReturn(notificationEntityPage);
        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));
        Mockito.when(notificationContentRepository.findMatchingNotification(Mockito.any(), Mockito.any())).thenReturn(notificationEntityPage);

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, null);
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
    public void findByIdsTest() throws Exception {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findAllById(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, null);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.findByIds(List.of(1L));

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel alertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel);
    }

    @Test
    public void findByIdTest() throws Exception {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findById(Mockito.any())).thenReturn(Optional.of(notificationEntity));
        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, null);
        Optional<AlertNotificationModel> alertNotificationModel = notificationManager.findById(1L);

        assertTrue(alertNotificationModel.isPresent());
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel.get());
    }

    @Test
    public void findByCreatedAtBetweenTest() throws Exception {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, null);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.findByCreatedAtBetween(DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp());

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel alertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel);
    }

    @Test
    public void findByCreatedAtBeforeTest() throws Exception {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findByCreatedAtBefore(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, null);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.findByCreatedAtBefore(DateUtils.createCurrentDateTimestamp());

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel alertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel);
    }

    @Test
    public void findByCreatedAtBeforeDayOffsetTest() throws Exception {
        NotificationEntity notificationEntity = new NotificationEntity(id, DateUtils.createCurrentDateTimestamp(), provider, providerConfigId, DateUtils.createCurrentDateTimestamp(), notificationType, content);
        ConfigurationModel configurationModel = createConfigurationModel();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(notificationContentRepository.findByCreatedAtBefore(Mockito.any())).thenReturn(List.of(notificationEntity));
        Mockito.when(configurationAccessor.getConfigurationById(Mockito.any())).thenReturn(Optional.of(configurationModel));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, null, null, configurationAccessor, null);
        List<AlertNotificationModel> alertNotificationModelList = notificationManager.findByCreatedAtBeforeDayOffset(1);

        assertEquals(1, alertNotificationModelList.size());
        AlertNotificationModel alertNotificationModel = alertNotificationModelList.get(0);
        testExpectedAlertNotificationModel(expectedAlertNotificationModel, alertNotificationModel);
    }

    @Test
    public void deleteNotificationListTest() {
        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(null, providerConfigId, provider, providerConfigName, notificationType, content, DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp());
        AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(1L, 2L);
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(UUID.randomUUID(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), "test-status", "test-errorMessage", "test-errorStackTrace");

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(auditNotificationRepository.findByNotificationId(Mockito.anyLong())).thenReturn(List.of(auditNotificationRelation));
        Mockito.when(auditEntryRepository.findAllById(Mockito.any())).thenReturn(List.of(auditEntryEntity));

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, auditEntryRepository, auditNotificationRepository, configurationAccessor, null);
        notificationManager.deleteNotificationList(List.of(alertNotificationModel));

        Mockito.verify(auditEntryRepository).deleteAll(Mockito.any());
        Mockito.verify(notificationContentRepository).deleteById(Mockito.any());
    }

    @Test
    public void getPageRequestForNotificationsTest() {
        final int pageNumber = 1;
        final int pageSize = 1;
        final String sortField = "content";
        String sortOrder = Sort.Direction.ASC.name();

        NotificationContentRepository notificationContentRepository = Mockito.mock(NotificationContentRepository.class);
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        DefaultNotificationManager notificationManager = new DefaultNotificationManager(notificationContentRepository, auditEntryRepository, auditNotificationRepository, configurationAccessor, null);
        PageRequest pageRequest = notificationManager.getPageRequestForNotifications(pageNumber, pageSize, sortField, sortOrder);

        assertEquals(pageNumber, pageRequest.getPageNumber());
        assertEquals(pageSize, pageRequest.getPageSize());
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

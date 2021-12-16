package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.job.DistributionJobEntity;
import com.synopsys.integration.alert.database.job.DistributionJobRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
public class NotificationAccessorTestIT {
    private static final String NOTIFICATION_TYPE = "notificationType";

    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private AuditEntryRepository auditEntryRepository;

    @Autowired
    private DistributionJobRepository distributionJobRepository;

    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;

    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    @Autowired
    private DefaultNotificationAccessor notificationManager;

    private ConfigurationModel providerConfigModel = null;

    public void assertNotificationModel(AlertNotificationModel notification, AlertNotificationModel savedNotification) {
        assertEquals(notification.getCreatedAt(), savedNotification.getCreatedAt());
        assertEquals(notification.getProvider(), savedNotification.getProvider());
        assertEquals(notification.getNotificationType(), savedNotification.getNotificationType());
        assertEquals(notification.getContent(), savedNotification.getContent());
    }

    @BeforeEach
    public void init() {
        cleanDB();

        ConfigurationFieldModel providerConfigEnabled = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        providerConfigEnabled.setFieldValue("true");
        ConfigurationFieldModel providerConfigName = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        providerConfigName.setFieldValue("My Black Duck Config");

        ConfigurationFieldModel blackduckUrl = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_URL);
        blackduckUrl.setFieldValue("https://a-blackduck-server");
        ConfigurationFieldModel blackduckApiKey = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY);
        blackduckApiKey.setFieldValue("123456789012345678901234567890123456789012345678901234567890");
        ConfigurationFieldModel blackduckTimeout = ConfigurationFieldModel.create(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT);
        blackduckTimeout.setFieldValue("300");

        List<ConfigurationFieldModel> providerConfigFields = List.of(providerConfigEnabled, providerConfigName, blackduckUrl, blackduckApiKey, blackduckTimeout);
        providerConfigModel = configurationModelConfigurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields);
    }

    @AfterEach
    public void cleanUpDB() {
        configurationModelConfigurationAccessor.deleteConfiguration(providerConfigModel.getConfigurationId());
        cleanDB();
    }

    private void cleanDB() {
        notificationContentRepository.flush();
        notificationContentRepository.flush();
        auditNotificationRepository.flush();
        auditEntryRepository.flush();
        descriptorConfigRepository.flush();
        fieldValueRepository.flush();
        notificationContentRepository.deleteAllInBatch();
        notificationContentRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        auditEntryRepository.deleteAllInBatch();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
    }

    @Test
    public void testFindAllEmpty() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AlertNotificationModel> all = notificationManager.findAll(pageRequest, false);
        assertTrue(all.isEmpty());

        all = notificationManager.findAll(pageRequest, true);
        assertTrue(all.isEmpty());
    }

    @Test
    public void testFindAll() {
        NotificationEntity notificationContent = createNotificationContent();
        notificationContent = notificationContentRepository.save(notificationContent);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AlertNotificationModel> all = notificationManager.findAll(pageRequest, false);
        assertFalse(all.isEmpty());

        all = notificationManager.findAll(pageRequest, true);
        assertTrue(all.isEmpty());

        OffsetDateTime now = DateUtils.createCurrentDateTimestamp();
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(UUID.randomUUID(), now, now, AuditEntryStatus.PENDING.name(), null, null);
        AuditEntryEntity saveAuditEntry = auditEntryRepository.save(auditEntryEntity);

        AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(saveAuditEntry.getId(), notificationContent.getId());
        auditNotificationRepository.save(auditNotificationRelation);

        all = notificationManager.findAll(pageRequest, true);
        assertFalse(all.isEmpty());
    }

    @Test
    public void testFindAllWithSearchEmpty() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AlertNotificationModel> all = notificationManager.findAllWithSearch(ChannelKeys.EMAIL.getUniversalKey(), pageRequest, false);
        assertTrue(all.isEmpty());

        all = notificationManager.findAllWithSearch(ChannelKeys.EMAIL.getUniversalKey(), pageRequest, true);
        assertTrue(all.isEmpty());
    }

    @Test
    public void testFindAllWithSearch() {
        NotificationEntity notificationContent = createNotificationContent();
        notificationContent = notificationContentRepository.save(notificationContent);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AlertNotificationModel> all = notificationManager.findAllWithSearch(ChannelKeys.EMAIL.getUniversalKey(), pageRequest, false);
        // Search term should not match anything in the saved notifications
        assertTrue(all.isEmpty());

        all = notificationManager.findAllWithSearch(NOTIFICATION_TYPE, pageRequest, false);
        // Search term should match the notification type of the saved notification
        assertFalse(all.isEmpty());

        all = notificationManager.findAllWithSearch(NOTIFICATION_TYPE, pageRequest, true);
        // Search term should match the notification type but it was never sent so no match
        assertTrue(all.isEmpty());

        OffsetDateTime now = DateUtils.createCurrentDateTimestamp();
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(UUID.randomUUID(), now, now, AuditEntryStatus.PENDING.name(), null, null);
        AuditEntryEntity saveAuditEntry = auditEntryRepository.save(auditEntryEntity);

        AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(saveAuditEntry.getId(), notificationContent.getId());
        auditNotificationRepository.save(auditNotificationRelation);

        all = notificationManager.findAllWithSearch(NOTIFICATION_TYPE, pageRequest, true);
        // Search term should match the notification type and the notification was sent so we get a match
        assertFalse(all.isEmpty());
    }

    @Test
    public void testFindAllWithSearchByFieldValue() {
        NotificationEntity notificationContent = createNotificationContent();
        notificationContent = notificationContentRepository.save(notificationContent);

        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        DistributionJobEntity distributionJobEntity = new DistributionJobEntity(null, "job_name", true, FrequencyType.REAL_TIME.name(), ProcessingType.DEFAULT.name(), ChannelKeys.EMAIL.getUniversalKey(), currentTime, null);
        DistributionJobEntity savedJob = distributionJobRepository.save(distributionJobEntity);

        final String auditStatus = "audit status thing";
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(savedJob.getJobId(), DateUtils.createCurrentDateTimestamp(), DateUtils.createCurrentDateTimestamp(), auditStatus, "", "");
        auditEntryEntity = auditEntryRepository.save(auditEntryEntity);

        AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(auditEntryEntity.getId(), notificationContent.getId());
        auditNotificationRepository.save(auditNotificationRelation);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AlertNotificationModel> all = notificationManager.findAllWithSearch(ChannelKeys.EMAIL.getUniversalKey(), pageRequest, false);
        // Search term should match the channel name
        assertFalse(all.isEmpty());
    }

    @Test
    public void testSave() {
        AlertNotificationModel notificationContent = createNotificationModel();
        List<AlertNotificationModel> savedModels = notificationManager.saveAllNotifications(List.of(notificationContent));
        assertNotNull(savedModels);
        assertFalse(savedModels.isEmpty());
        AlertNotificationModel savedModel = savedModels.get(0);
        assertNotNull(savedModel.getId());
        assertNotificationModel(notificationContent, savedModel);
    }

    @Test
    public void testFindByIds() {
        AlertNotificationModel notification = createNotificationModel();
        List<AlertNotificationModel> savedModels = notificationManager.saveAllNotifications(List.of(notification));
        List<Long> notificationIds = savedModels.stream().map(AlertNotificationModel::getId).collect(Collectors.toList());
        List<AlertNotificationModel> notificationList = notificationManager.findByIds(notificationIds);

        assertEquals(1, notificationList.size());
    }

    @Test
    public void testFindByIdsInvalidIds() {
        AlertNotificationModel model = createNotificationModel();
        model = notificationManager.saveAllNotifications(List.of(model)).get(0);

        List<Long> notificationIds = Arrays.asList(model.getId() + 10, model.getId() + 20, model.getId() + 30);
        List<AlertNotificationModel> notificationModelList = notificationManager.findByIds(notificationIds);
        assertTrue(notificationModelList.isEmpty());
    }

    @Test
    public void findByCreatedAtBetween() {
        OffsetDateTime time = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime startDate = time.minusHours(1);
        OffsetDateTime endDate = time.plusHours(1);
        OffsetDateTime createdAt = time.minusHours(3);
        AlertNotificationModel entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        createdAt = time.plusMinutes(1);
        AlertNotificationModel entityToFind1 = createNotificationModel(createdAt);
        createdAt = time.plusMinutes(5);
        AlertNotificationModel entityToFind2 = createNotificationModel(createdAt);
        createdAt = time.plusHours(3);
        entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        notificationManager.saveAllNotifications(List.of(entityToFind1));
        notificationManager.saveAllNotifications(List.of(entityToFind2));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE).getModels();

        assertEquals(2, foundList.size());
        assertNotificationModel(entityToFind1, foundList.get(0));
        assertNotificationModel(entityToFind2, foundList.get(1));
    }

    @Test
    public void findByCreatedAtBetweenInvalidDate() {
        OffsetDateTime time = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime startDate = time.minusHours(1);
        OffsetDateTime endDate = time.plusHours(1);
        OffsetDateTime createdAtEarlier = time.minusHours(5);
        AlertNotificationModel entity = createNotificationModel(createdAtEarlier);
        notificationManager.saveAllNotifications(List.of(entity));

        OffsetDateTime createdAtLater = time.plusHours(3);
        entity = createNotificationModel(createdAtLater);
        notificationManager.saveAllNotifications(List.of(entity));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate, AlertPagedModel.DEFAULT_PAGE_NUMBER, AlertPagedModel.DEFAULT_PAGE_SIZE).getModels();

        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBefore() {
        OffsetDateTime time = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime searchDate = time.plusHours(1);
        OffsetDateTime createdAt = time.minusHours(5);
        AlertNotificationModel entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        OffsetDateTime createdAtLaterThanSearch = time.plusHours(3);
        entity = createNotificationModel(createdAtLaterThanSearch);
        notificationManager.saveAllNotifications(List.of(entity));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBefore(searchDate);

        assertEquals(1, foundList.size());

        searchDate = time.minusHours(6);
        foundList = notificationManager.findByCreatedAtBefore(searchDate);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBeforeDayOffset() {
        OffsetDateTime time = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime createdAt = time.minusDays(5);
        AlertNotificationModel entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        OffsetDateTime createdAtLaterThanSearch = time.plusDays(3);
        entity = createNotificationModel(createdAtLaterThanSearch);
        notificationManager.saveAllNotifications(List.of(entity));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBeforeDayOffset(2);

        assertEquals(1, foundList.size());

        foundList = notificationManager.findByCreatedAtBeforeDayOffset(6);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void deleteNotificationsCreatedBeforeTest() {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime oneHourAgo = currentTime.minusHours(1);
        OffsetDateTime oneAndAHalfHoursAgo = oneHourAgo.minusMinutes(30);
        OffsetDateTime twoHoursAgo = currentTime.minusHours(2);
        OffsetDateTime threeHoursAgo = currentTime.minusHours(3);
        OffsetDateTime oneDayAgo = currentTime.minusDays(1);

        NotificationEntity notification1 = createNotificationContent(oneHourAgo);
        // These notifications should be deleted
        NotificationEntity notification2 = createNotificationContent(twoHoursAgo);
        NotificationEntity notification3 = createNotificationContent(threeHoursAgo);
        NotificationEntity notification4 = createNotificationContent(oneDayAgo);

        notificationContentRepository.saveAll(List.of(notification1, notification2, notification3, notification4));

        int deletedCount = notificationManager.deleteNotificationsCreatedBefore(oneAndAHalfHoursAgo);
        assertEquals(3, deletedCount);

        List<NotificationEntity> remainingNotifications = notificationContentRepository.findAll();
        assertEquals(1, remainingNotifications.size());

        NotificationEntity remainingNotification = remainingNotifications.get(0);
        assertEquals(notification1.getCreatedAt(), remainingNotification.getCreatedAt());
    }

    @Test
    public void testDeleteNotification() {
        AlertNotificationModel notificationEntity = createNotificationModel();
        AlertNotificationModel savedModel = notificationManager.saveAllNotifications(List.of(notificationEntity)).get(0);
        assertEquals(1, notificationContentRepository.count());

        notificationManager.deleteNotification(savedModel);

        assertEquals(0, notificationContentRepository.count());
    }

    @Test
    public void setNotificationsProcessedTest() {
        AlertNotificationModel notificationModel = createNotificationModel();

        List<AlertNotificationModel> savedModels = notificationManager.saveAllNotifications(List.of(notificationModel));

        notificationManager.setNotificationsProcessed(savedModels);

        assertEquals(1, savedModels.size());
        Optional<AlertNotificationModel> alertNotificationModelTest = notificationManager.findById(savedModels.get(0).getId());
        assertTrue(alertNotificationModelTest.isPresent());
        assertTrue(alertNotificationModelTest.get().getProcessed());
    }

    @Test
    public void setNotificationsProcessedByIdTest() {
        AlertNotificationModel notificationModel = createNotificationModel();

        List<AlertNotificationModel> savedModels = notificationManager.saveAllNotifications(List.of(notificationModel));
        List<Long> notificationIds = savedModels
            .stream()
            .map(AlertNotificationModel::getId)
            .collect(Collectors.toList());

        notificationManager.setNotificationsProcessedById(new HashSet<>(notificationIds));

        assertEquals(1, notificationIds.size());
        Optional<AlertNotificationModel> alertNotificationModelTest = notificationManager.findById(notificationIds.get(0));
        assertTrue(alertNotificationModelTest.isPresent());
        assertTrue(alertNotificationModelTest.get().getProcessed());
    }

    private AlertNotificationModel createNotificationModel(OffsetDateTime createdAt) {
        return new AlertNotificationModel(1L, providerConfigModel.getConfigurationId(), "provider", "providerConfigName", NOTIFICATION_TYPE, "{content: \"content is here...\"}", createdAt, createdAt, false);
    }

    private AlertNotificationModel createNotificationModel() {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        return createNotificationModel(createdAt);
    }

    private NotificationEntity createNotificationContent(OffsetDateTime createdAt) {
        MockNotificationContent mockedNotificationContent = new MockNotificationContent(createdAt, "provider", createdAt, NOTIFICATION_TYPE, "{content: \"content is here...\"}", null, providerConfigModel.getConfigurationId());
        return mockedNotificationContent.createEntity();
    }

    private NotificationEntity createNotificationContent() {
        OffsetDateTime createdAt = DateUtils.createCurrentDateTimestamp();
        return createNotificationContent(createdAt);
    }

}

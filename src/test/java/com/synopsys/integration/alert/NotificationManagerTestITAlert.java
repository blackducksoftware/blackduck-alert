package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.channel.email.EmailChannelKey;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultNotificationManager;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.ConfigContextRepository;
import com.synopsys.integration.alert.database.configuration.repository.ConfigGroupRepository;
import com.synopsys.integration.alert.database.configuration.repository.DefinedFieldRepository;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
public class NotificationManagerTestITAlert extends AlertIntegrationTest {
    private static final String NOTIFICATION_TYPE = "notificationType";

    @Autowired
    private NotificationContentRepository notificationContentRepository;
    @Autowired
    private AuditNotificationRepository auditNotificationRepository;
    @Autowired
    private AuditEntryRepository auditEntryRepository;
    @Autowired
    private ConfigGroupRepository configGroupRepository;
    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;
    @Autowired
    private RegisteredDescriptorRepository registeredDescriptorRepository;
    @Autowired
    private ConfigContextRepository configContextRepository;
    @Autowired
    private FieldValueRepository fieldValueRepository;
    @Autowired
    private DefinedFieldRepository definedFieldRepository;
    @Autowired
    private EmailChannelKey emailChannelKey;

    @Autowired
    private ConfigurationAccessor configurationAccessor;
    @Autowired
    private DefaultNotificationManager notificationManager;

    private ConfigurationModel providerConfigModel = null;

    public void assertNotificationModel(AlertNotificationModel notification, AlertNotificationModel savedNotification) {
        assertEquals(notification.getCreatedAt(), savedNotification.getCreatedAt());
        assertEquals(notification.getProvider(), savedNotification.getProvider());
        assertEquals(notification.getNotificationType(), savedNotification.getNotificationType());
        assertEquals(notification.getContent(), savedNotification.getContent());
    }

    @BeforeEach
    public void init() throws AlertDatabaseConstraintException {
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
        providerConfigModel = configurationAccessor.createConfiguration(new BlackDuckProviderKey(), ConfigContextEnum.GLOBAL, providerConfigFields);
    }

    @AfterEach
    public void cleanUpDB() throws AlertDatabaseConstraintException {
        configurationAccessor.deleteConfiguration(providerConfigModel.getConfigurationId());
        cleanDB();
    }

    private void cleanDB() {
        notificationContentRepository.flush();
        notificationContentRepository.flush();
        auditNotificationRepository.flush();
        auditEntryRepository.flush();
        configGroupRepository.flush();
        descriptorConfigRepository.flush();
        fieldValueRepository.flush();
        notificationContentRepository.deleteAllInBatch();
        notificationContentRepository.deleteAllInBatch();
        auditNotificationRepository.deleteAllInBatch();
        auditEntryRepository.deleteAllInBatch();
        configGroupRepository.deleteAllInBatch();
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

        Date now = new Date();
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
        Page<AlertNotificationModel> all = notificationManager.findAllWithSearch(emailChannelKey.getUniversalKey(), pageRequest, false);
        assertTrue(all.isEmpty());

        all = notificationManager.findAllWithSearch(emailChannelKey.getUniversalKey(), pageRequest, true);
        assertTrue(all.isEmpty());
    }

    @Test
    public void testFindAllWithSearch() {
        NotificationEntity notificationContent = createNotificationContent();
        notificationContent = notificationContentRepository.save(notificationContent);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AlertNotificationModel> all = notificationManager.findAllWithSearch(emailChannelKey.getUniversalKey(), pageRequest, false);
        // Search term should not match anything in the saved notifications
        assertTrue(all.isEmpty());

        all = notificationManager.findAllWithSearch(NOTIFICATION_TYPE, pageRequest, false);
        // Search term should match the notification type of the saved notification
        assertFalse(all.isEmpty());

        all = notificationManager.findAllWithSearch(NOTIFICATION_TYPE, pageRequest, true);
        // Search term should match the notification type but it was never sent so no match
        assertTrue(all.isEmpty());

        Date now = new Date();
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

        UUID jobId = UUID.randomUUID();

        RegisteredDescriptorEntity registeredDescriptorEntity = registeredDescriptorRepository.findFirstByName(emailChannelKey.getUniversalKey()).orElse(null);
        ConfigContextEntity configContextEntity = configContextRepository.findFirstByContext(ConfigContextEnum.GLOBAL.name()).orElse(null);

        Date currentTime = DateUtils.createCurrentDateTimestamp();
        DescriptorConfigEntity descriptorConfig = new DescriptorConfigEntity(registeredDescriptorEntity.getId(), configContextEntity.getId(), currentTime, currentTime);
        descriptorConfig = descriptorConfigRepository.save(descriptorConfig);

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(descriptorConfig.getId(), jobId);
        configGroupRepository.save(configGroupEntity);

        DefinedFieldEntity definedFieldEntity = definedFieldRepository.findFirstByKey(ChannelDistributionUIConfig.KEY_CHANNEL_NAME).orElse(null);

        FieldValueEntity fieldValueEntity = new FieldValueEntity(descriptorConfig.getId(), definedFieldEntity.getId(), emailChannelKey.getUniversalKey());
        fieldValueRepository.save(fieldValueEntity);

        final String auditStatus = "audit status thing";
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(jobId, new Date(), new Date(), auditStatus, "", "");
        auditEntryEntity = auditEntryRepository.save(auditEntryEntity);

        AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(auditEntryEntity.getId(), notificationContent.getId());
        auditNotificationRepository.save(auditNotificationRelation);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AlertNotificationModel> all = notificationManager.findAllWithSearch(emailChannelKey.getUniversalKey(), pageRequest, false);
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
        LocalDateTime time = LocalDateTime.now();
        Date startDate = createDate(time.minusHours(1));
        Date endDate = createDate(time.plusHours(1));
        Date createdAt = createDate(time.minusHours(3));
        AlertNotificationModel entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        createdAt = createDate(time.plusMinutes(1));
        AlertNotificationModel entityToFind1 = createNotificationModel(createdAt);
        createdAt = createDate(time.plusMinutes(5));
        AlertNotificationModel entityToFind2 = createNotificationModel(createdAt);
        createdAt = createDate(time.plusHours(3));
        entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        notificationManager.saveAllNotifications(List.of(entityToFind1));
        notificationManager.saveAllNotifications(List.of(entityToFind2));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);

        assertEquals(2, foundList.size());
        assertNotificationModel(entityToFind1, foundList.get(0));
        assertNotificationModel(entityToFind2, foundList.get(1));
    }

    @Test
    public void findByCreatedAtBetweenInvalidDate() {
        LocalDateTime time = LocalDateTime.now();
        Date startDate = createDate(time.minusHours(1));
        Date endDate = createDate(time.plusHours(1));
        Date createdAtEarlier = createDate(time.minusHours(5));
        AlertNotificationModel entity = createNotificationModel(createdAtEarlier);
        notificationManager.saveAllNotifications(List.of(entity));

        Date createdAtLater = createDate(time.plusHours(3));
        entity = createNotificationModel(createdAtLater);
        notificationManager.saveAllNotifications(List.of(entity));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);

        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBefore() {
        LocalDateTime time = LocalDateTime.now();
        Date searchDate = createDate(time.plusHours(1));
        Date createdAt = createDate(time.minusHours(5));
        AlertNotificationModel entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        Date createdAtLaterThanSearch = createDate(time.plusHours(3));
        entity = createNotificationModel(createdAtLaterThanSearch);
        notificationManager.saveAllNotifications(List.of(entity));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBefore(searchDate);

        assertEquals(1, foundList.size());

        searchDate = createDate(time.minusHours(6));
        foundList = notificationManager.findByCreatedAtBefore(searchDate);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBeforeDayOffset() {
        LocalDateTime time = LocalDateTime.now();
        Date createdAt = createDate(time.minusDays(5));
        AlertNotificationModel entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        Date createdAtLaterThanSearch = createDate(time.plusDays(3));
        entity = createNotificationModel(createdAtLaterThanSearch);
        notificationManager.saveAllNotifications(List.of(entity));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBeforeDayOffset(2);

        assertEquals(1, foundList.size());

        foundList = notificationManager.findByCreatedAtBeforeDayOffset(6);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void testDeleteNotificationList() {
        LocalDateTime time = LocalDateTime.now();
        Date startDate = createDate(time.minusHours(1));
        Date endDate = createDate(time.plusHours(1));
        Date createdAt = createDate(time.minusHours(3));
        AlertNotificationModel entity = createNotificationModel(createdAt);
        notificationManager.saveAllNotifications(List.of(entity));
        Date createdAtInRange = createDate(time.plusMinutes(1));
        AlertNotificationModel entityToFind1 = createNotificationModel(createdAtInRange);
        createdAtInRange = createDate(time.plusMinutes(5));
        AlertNotificationModel entityToFind2 = createNotificationModel(createdAtInRange);
        Date createdAtLater = createDate(time.plusHours(3));
        entity = createNotificationModel(createdAtLater);
        notificationManager.saveAllNotifications(List.of(entity));
        notificationManager.saveAllNotifications(List.of(entityToFind1));
        notificationManager.saveAllNotifications(List.of(entityToFind2));

        List<AlertNotificationModel> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);
        assertEquals(4, notificationContentRepository.count());

        notificationManager.deleteNotificationList(foundList);

        assertEquals(2, notificationContentRepository.count());
    }

    @Test
    public void testDeleteNotification() {
        AlertNotificationModel notificationEntity = createNotificationModel();
        AlertNotificationModel savedModel = notificationManager.saveAllNotifications(List.of(notificationEntity)).get(0);
        assertEquals(1, notificationContentRepository.count());

        notificationManager.deleteNotification(savedModel);

        assertEquals(0, notificationContentRepository.count());
    }

    private AlertNotificationModel createNotificationModel(Date createdAt) {
        return new AlertNotificationModel(1L, providerConfigModel.getConfigurationId(), "provider", "providerConfigName", NOTIFICATION_TYPE, "{content: \"content is here...\"}", createdAt, createdAt);
    }

    private AlertNotificationModel createNotificationModel() {
        Date createdAt = createDate(LocalDateTime.now());
        return createNotificationModel(createdAt);
    }

    private NotificationEntity createNotificationContent(Date createdAt) {
        MockNotificationContent mockedNotificationContent = new MockNotificationContent(createdAt, "provider", createdAt, NOTIFICATION_TYPE, "{content: \"content is here...\"}", null, providerConfigModel.getConfigurationId());
        return mockedNotificationContent.createEntity();
    }

    private NotificationEntity createNotificationContent() {
        Date createdAt = createDate(LocalDateTime.now());
        return createNotificationContent(createdAt);
    }

    private Date createDate(LocalDateTime localTime) {
        return Date.from(localTime.toInstant(ZoneOffset.UTC));
    }

}

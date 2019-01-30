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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.audit.relation.AuditNotificationRelation;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.entity.configuration.ConfigContextEntity;
import com.synopsys.integration.alert.database.entity.configuration.ConfigGroupEntity;
import com.synopsys.integration.alert.database.entity.configuration.DefinedFieldEntity;
import com.synopsys.integration.alert.database.entity.configuration.DescriptorConfigEntity;
import com.synopsys.integration.alert.database.entity.configuration.FieldValueEntity;
import com.synopsys.integration.alert.database.entity.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.entity.repository.NotificationContentRepository;
import com.synopsys.integration.alert.database.repository.configuration.ConfigContextRepository;
import com.synopsys.integration.alert.database.repository.configuration.ConfigGroupRepository;
import com.synopsys.integration.alert.database.repository.configuration.DefinedFieldRepository;
import com.synopsys.integration.alert.database.repository.configuration.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.repository.configuration.FieldValueRepository;
import com.synopsys.integration.alert.database.repository.configuration.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.mock.entity.MockNotificationContent;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.workflow.NotificationManager;

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
    private NotificationManager notificationManager;

    public void assertNotificationModel(final NotificationContent notification, final NotificationContent savedNotification) {
        assertEquals(notification.getCreatedAt(), savedNotification.getCreatedAt());
        assertEquals(notification.getProvider(), savedNotification.getProvider());
        assertEquals(notification.getNotificationType(), savedNotification.getNotificationType());
        assertEquals(notification.getContent(), savedNotification.getContent());
    }

    @BeforeEach
    public void init() {
        cleanDB();
    }

    @AfterEach
    public void cleanUpDB() {
        cleanDB();
    }

    private void cleanDB() {
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
        final PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationContent> all = notificationManager.findAll(pageRequest, false);
        assertTrue(all.isEmpty());

        all = notificationManager.findAll(pageRequest, true);
        assertTrue(all.isEmpty());
    }

    @Test
    public void testFindAll() {
        NotificationContent notificationContent = createNotificationContent();
        notificationContent = notificationContentRepository.save(notificationContent);

        final PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationContent> all = notificationManager.findAll(pageRequest, false);
        assertFalse(all.isEmpty());

        all = notificationManager.findAll(pageRequest, true);
        assertTrue(all.isEmpty());

        final AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(0L, notificationContent.getId());
        auditNotificationRepository.save(auditNotificationRelation);

        all = notificationManager.findAll(pageRequest, true);
        assertFalse(all.isEmpty());
    }

    @Test
    public void testFindAllWithSearchEmpty() {
        final PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationContent> all = notificationManager.findAllWithSearch(EmailChannel.COMPONENT_NAME, pageRequest, false);
        assertTrue(all.isEmpty());

        all = notificationManager.findAllWithSearch(EmailChannel.COMPONENT_NAME, pageRequest, true);
        assertTrue(all.isEmpty());
    }

    @Test
    public void testFindAllWithSearch() {
        NotificationContent notificationContent = createNotificationContent();
        notificationContent = notificationContentRepository.save(notificationContent);

        final PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationContent> all = notificationManager.findAllWithSearch(EmailChannel.COMPONENT_NAME, pageRequest, false);
        // Search term should not match anything in the saved notifications
        assertTrue(all.isEmpty());

        all = notificationManager.findAllWithSearch(NOTIFICATION_TYPE, pageRequest, false);
        // Search term should match the notification type of the saved notification
        assertFalse(all.isEmpty());

        all = notificationManager.findAllWithSearch(NOTIFICATION_TYPE, pageRequest, true);
        // Search term should match the notification type but it was never sent so no match
        assertTrue(all.isEmpty());

        final AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(0L, notificationContent.getId());
        auditNotificationRepository.save(auditNotificationRelation);

        all = notificationManager.findAllWithSearch(NOTIFICATION_TYPE, pageRequest, true);
        // Search term should match the notification type and the notification was sent so we get a match
        assertFalse(all.isEmpty());
    }

    @Test
    public void testFindAllWithSearchByFieldValue() {
        NotificationContent notificationContent = createNotificationContent();
        notificationContent = notificationContentRepository.save(notificationContent);

        final UUID jobId = UUID.randomUUID();

        final RegisteredDescriptorEntity registeredDescriptorEntity = registeredDescriptorRepository.findFirstByName(EmailChannel.COMPONENT_NAME).orElse(null);
        final ConfigContextEntity configContextEntity = configContextRepository.findFirstByContext(ConfigContextEnum.GLOBAL.name()).orElse(null);

        DescriptorConfigEntity descriptorConfig = new DescriptorConfigEntity(registeredDescriptorEntity.getId(), configContextEntity.getId());
        descriptorConfig = descriptorConfigRepository.save(descriptorConfig);

        ConfigGroupEntity configGroupEntity = new ConfigGroupEntity(descriptorConfig.getId(), jobId);
        configGroupEntity = configGroupRepository.save(configGroupEntity);

        final DefinedFieldEntity definedFieldEntity = definedFieldRepository.findFirstByKey(CommonDistributionUIConfig.KEY_CHANNEL_NAME).orElse(null);

        FieldValueEntity fieldValueEntity = new FieldValueEntity(descriptorConfig.getId(), definedFieldEntity.getId(), EmailChannel.COMPONENT_NAME);
        fieldValueEntity = fieldValueRepository.save(fieldValueEntity);

        final String auditStatus = "audit status thing";
        AuditEntryEntity auditEntryEntity = new AuditEntryEntity(jobId, new Date(), new Date(), auditStatus, "", "");
        auditEntryEntity = auditEntryRepository.save(auditEntryEntity);

        final AuditNotificationRelation auditNotificationRelation = new AuditNotificationRelation(auditEntryEntity.getId(), notificationContent.getId());
        auditNotificationRepository.save(auditNotificationRelation);

        final PageRequest pageRequest = PageRequest.of(0, 10);
        final Page<NotificationContent> all = notificationManager.findAllWithSearch(EmailChannel.COMPONENT_NAME, pageRequest, false);
        // Search term should match the channel name
        assertFalse(all.isEmpty());
    }

    @Test
    public void testSave() {
        final NotificationContent notificationContent = createNotificationContent();
        final NotificationContent savedModel = notificationManager.saveNotification(notificationContent);
        assertNotNull(savedModel.getId());
        assertNotificationModel(notificationContent, savedModel);
    }

    @Test
    public void testFindByIds() {
        final NotificationContent notification = createNotificationContent();
        final NotificationContent savedModel = notificationManager.saveNotification(notification);
        final List<Long> notificationIds = Arrays.asList(savedModel.getId());
        final List<NotificationContent> notificationList = notificationManager.findByIds(notificationIds);

        assertEquals(1, notificationList.size());
    }

    @Test
    public void testFindByIdsInvalidIds() {
        NotificationContent model = createNotificationContent();
        model = notificationManager.saveNotification(model);

        final List<Long> notificationIds = Arrays.asList(model.getId() + 10, model.getId() + 20, model.getId() + 30);
        final List<NotificationContent> notificationModelList = notificationManager.findByIds(notificationIds);
        assertTrue(notificationModelList.isEmpty());
    }

    @Test
    public void findByCreatedAtBetween() {
        final LocalDateTime time = LocalDateTime.now();
        final Date startDate = createDate(time.minusHours(1));
        final Date endDate = createDate(time.plusHours(1));
        Date createdAt = createDate(time.minusHours(3));
        NotificationContent entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        createdAt = createDate(time.plusMinutes(1));
        final NotificationContent entityToFind1 = createNotificationContent(createdAt);
        createdAt = createDate(time.plusMinutes(5));
        final NotificationContent entityToFind2 = createNotificationContent(createdAt);
        createdAt = createDate(time.plusHours(3));
        entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        notificationManager.saveNotification(entityToFind1);
        notificationManager.saveNotification(entityToFind2);

        final List<NotificationContent> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);

        assertEquals(2, foundList.size());
        assertNotificationModel(entityToFind1, foundList.get(0));
        assertNotificationModel(entityToFind2, foundList.get(1));
    }

    @Test
    public void findByCreatedAtBetweenInvalidDate() {
        final LocalDateTime time = LocalDateTime.now();
        final Date startDate = createDate(time.minusHours(1));
        final Date endDate = createDate(time.plusHours(1));
        final Date createdAtEarlier = createDate(time.minusHours(5));
        NotificationContent entity = createNotificationContent(createdAtEarlier);
        notificationManager.saveNotification(entity);

        final Date createdAtLater = createDate(time.plusHours(3));
        entity = createNotificationContent(createdAtLater);
        notificationManager.saveNotification(entity);

        final List<NotificationContent> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);

        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBefore() {
        final LocalDateTime time = LocalDateTime.now();
        Date searchDate = createDate(time.plusHours(1));
        final Date createdAt = createDate(time.minusHours(5));
        NotificationContent entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        final Date createdAtLaterThanSearch = createDate(time.plusHours(3));
        entity = createNotificationContent(createdAtLaterThanSearch);
        notificationManager.saveNotification(entity);

        List<NotificationContent> foundList = notificationManager.findByCreatedAtBefore(searchDate);

        assertEquals(1, foundList.size());

        searchDate = createDate(time.minusHours(6));
        foundList = notificationManager.findByCreatedAtBefore(searchDate);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void findByCreatedAtBeforeDayOffset() {
        final LocalDateTime time = LocalDateTime.now();
        final Date createdAt = createDate(time.minusDays(5));
        NotificationContent entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        final Date createdAtLaterThanSearch = createDate(time.plusDays(3));
        entity = createNotificationContent(createdAtLaterThanSearch);
        notificationManager.saveNotification(entity);

        List<NotificationContent> foundList = notificationManager.findByCreatedAtBeforeDayOffset(2);

        assertEquals(1, foundList.size());

        foundList = notificationManager.findByCreatedAtBeforeDayOffset(6);
        assertTrue(foundList.isEmpty());
    }

    @Test
    public void testDeleteNotificationList() {
        final LocalDateTime time = LocalDateTime.now();
        final Date startDate = createDate(time.minusHours(1));
        final Date endDate = createDate(time.plusHours(1));
        final Date createdAt = createDate(time.minusHours(3));
        NotificationContent entity = createNotificationContent(createdAt);
        notificationManager.saveNotification(entity);
        Date createdAtInRange = createDate(time.plusMinutes(1));
        final NotificationContent entityToFind1 = createNotificationContent(createdAtInRange);
        createdAtInRange = createDate(time.plusMinutes(5));
        final NotificationContent entityToFind2 = createNotificationContent(createdAtInRange);
        final Date createdAtLater = createDate(time.plusHours(3));
        entity = createNotificationContent(createdAtLater);
        notificationManager.saveNotification(entity);
        notificationManager.saveNotification(entityToFind1);
        notificationManager.saveNotification(entityToFind2);

        final List<NotificationContent> foundList = notificationManager.findByCreatedAtBetween(startDate, endDate);
        assertEquals(4, notificationContentRepository.count());

        notificationManager.deleteNotificationList(foundList);

        assertEquals(2, notificationContentRepository.count());
    }

    @Test
    public void testDeleteNotification() {
        final NotificationContent notificationEntity = createNotificationContent();
        final NotificationContent savedModel = notificationManager.saveNotification(notificationEntity);

        assertEquals(1, notificationContentRepository.count());

        notificationManager.deleteNotification(savedModel);

        assertEquals(0, notificationContentRepository.count());
    }

    private NotificationContent createNotificationContent(final Date createdAt) {
        final MockNotificationContent mockedNotificationContent = new MockNotificationContent(createdAt, "provider", createdAt, NOTIFICATION_TYPE, "{content: \"content is here...\"}", null);
        return mockedNotificationContent.createEntity();
    }

    private NotificationContent createNotificationContent() {
        final Date createdAt = createDate(LocalDateTime.now());
        return createNotificationContent(createdAt);
    }

    private Date createDate(final LocalDateTime localTime) {
        return Date.from(localTime.toInstant(ZoneOffset.UTC));
    }
}

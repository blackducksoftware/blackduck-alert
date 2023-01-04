package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.distribution.mock.MockAuditEntryRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditFailedEntryRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditFailedNotificationRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditNotificationRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockNotificationContentRepository;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingFailedAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultNotificationAccessor;
import com.synopsys.integration.alert.database.api.DefaultProcessingAuditAccessor;
import com.synopsys.integration.alert.database.api.DefaultProcessingFailedAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelationPK;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;
import com.synopsys.integration.alert.database.notification.NotificationContentRepository;
import com.synopsys.integration.alert.database.notification.NotificationEntity;

class AuditFailedHandlerTest {

    private ProcessingAuditAccessor processingAuditAccessor;
    private ProcessingFailedAccessor processingFailedAccessor;
    private AuditEntryRepository auditEntryRepository;
    private AuditNotificationRepository auditNotificationRepository;
    private AtomicLong idContainer = new AtomicLong(0L);

    private AuditFailedEntryRepository auditFailedEntryRepository;
    private AuditFailedNotificationRepository auditFailedNotificationRepository;

    private NotificationContentRepository notificationContentRepository;
    private final AtomicLong notificationIdContainer = new AtomicLong(0);

    @BeforeEach
    public void init() {
        auditNotificationRepository = new MockAuditNotificationRepository(this::generateRelationKey);
        auditEntryRepository = new MockAuditEntryRepository(this::generateEntityKey, auditNotificationRepository);
        processingAuditAccessor = new DefaultProcessingAuditAccessor(auditEntryRepository, auditNotificationRepository);
        notificationContentRepository = new MockNotificationContentRepository(this::generateNotificationId);
        auditFailedEntryRepository = new MockAuditFailedEntryRepository(AuditFailedEntity::getId);
        auditFailedNotificationRepository = new MockAuditFailedNotificationRepository(MockAuditFailedNotificationRepository::generateRelationKey);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        NotificationAccessor notificationAccessor = new DefaultNotificationAccessor(notificationContentRepository, auditEntryRepository, configurationModelConfigurationAccessor);
        processingFailedAccessor = new DefaultProcessingFailedAccessor(auditFailedEntryRepository, auditFailedNotificationRepository, notificationAccessor);

    }

    private Long generateNotificationId(NotificationEntity entity) {
        Long id = entity.getId();
        if (null == id) {
            id = notificationIdContainer.incrementAndGet();
            entity.setId(id);
        }
        return id;
    }

    private Long generateEntityKey(AuditEntryEntity entity) {
        Long id = entity.getId();
        if (null == id) {
            id = idContainer.incrementAndGet();
            entity.setId(id);
        }
        return id;
    }

    private AuditNotificationRelationPK generateRelationKey(AuditNotificationRelation relation) {
        AuditNotificationRelationPK key = new AuditNotificationRelationPK();
        key.setAuditEntryId(relation.getAuditEntryId());
        key.setNotificationId(relation.getNotificationId());
        return key;
    }

    @Test
    void handleEventTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        String stackTrace = "Stack trace goes here";

        AuditFailedHandler handler = new AuditFailedHandler(processingAuditAccessor, processingFailedAccessor);
        notificationIds.stream()
            .map(this::createNotification)
            .forEach(notificationContentRepository::save);
        processingAuditAccessor.createOrUpdatePendingAuditEntryForJob(jobId, notificationIds);
        AuditFailedEvent event = new AuditFailedEvent(jobId, notificationIds, errorMessage, stackTrace);

        handler.handle(event);

        for (Long notificationId : notificationIds) {
            List<AuditFailedNotificationRelation> relations = auditFailedNotificationRepository.findAuditFailedNotificationRelationsByNotificationId(notificationId);
            assertFalse(relations.isEmpty(), "Expect failure relations but none found");
            for (AuditFailedNotificationRelation relation : relations) {
                Optional<AuditFailedEntity> entry = auditFailedEntryRepository.findById(relation.getFailedAuditEntryId());
                assertTrue(entry.isPresent());
                AuditFailedEntity entity = entry.get();
                assertNotNull(entity.getId());
                assertNotNull(entity.getProviderId());
                assertEquals(event.getJobId(), entity.getJobConfigId());
                assertEquals(event.getCreatedTimestamp(), entity.getTimeCreated());
                assertEquals(errorMessage, entity.getErrorMessage());
                assertEquals(stackTrace, entity.getErrorStackTrace().orElseThrow(() -> new AssertionError("Expected stack trace but none found")));
            }
        }
    }

    @Test
    void handleEventAuditEntryMissingTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        String stackTrace = "Stack trace goes here";

        notificationIds.stream()
            .map(this::createNotification)
            .forEach(notificationContentRepository::save);
        AuditFailedHandler handler = new AuditFailedHandler(processingAuditAccessor, processingFailedAccessor);
        AuditFailedEvent event = new AuditFailedEvent(jobId, notificationIds, errorMessage, stackTrace);

        handler.handle(event);

        for (Long notificationId : notificationIds) {
            List<AuditFailedNotificationRelation> relations = auditFailedNotificationRepository.findAuditFailedNotificationRelationsByNotificationId(notificationId);
            assertFalse(relations.isEmpty(), "Expect failure relations but none found");
            for (AuditFailedNotificationRelation relation : relations) {
                Optional<AuditFailedEntity> entry = auditFailedEntryRepository.findById(relation.getFailedAuditEntryId());
                assertTrue(entry.isPresent());
                AuditFailedEntity entity = entry.get();
                assertNotNull(entity.getId());
                assertNotNull(entity.getProviderId());
                assertEquals(event.getJobId(), entity.getJobConfigId());
                assertEquals(event.getCreatedTimestamp(), entity.getTimeCreated());
                assertEquals(errorMessage, entity.getErrorMessage());
                assertEquals(stackTrace, entity.getErrorStackTrace().orElseThrow(() -> new AssertionError("Expected stack trace but none found")));
            }
        }
    }

    private NotificationEntity createNotification(Long id) {
        String provider = "Provider";
        String providerName = "My Provider";
        String content = "notification content";
        OffsetDateTime creationTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime providerCreationTime = creationTime.minus(1, ChronoUnit.MINUTES);
        return new NotificationEntity(
            id,
            creationTime,
            provider,
            1L,
            providerCreationTime,
            "VULNERABILITY",
            content,
            false
        );

    }
}

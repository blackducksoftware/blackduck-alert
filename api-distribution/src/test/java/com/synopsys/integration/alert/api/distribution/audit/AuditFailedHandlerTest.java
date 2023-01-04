package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.distribution.mock.MockAuditEntryRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditNotificationRepository;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.database.api.DefaultProcessingAuditAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelationPK;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

class AuditFailedHandlerTest {

    private ProcessingAuditAccessor processingAuditAccessor;
    private AuditEntryRepository auditEntryRepository;
    private AuditNotificationRepository auditNotificationRepository;
    private AtomicLong idContainer = new AtomicLong(0L);

    @BeforeEach
    public void init() {
        auditNotificationRepository = new MockAuditNotificationRepository(this::generateRelationKey);
        auditEntryRepository = new MockAuditEntryRepository(this::generateEntityKey, auditNotificationRepository);
        AuditFailedEntryRepository auditFailedEntryRepository = Mockito.mock(AuditFailedEntryRepository.class);
        AuditFailedNotificationRepository auditFailedNotificationRepository = Mockito.mock(AuditFailedNotificationRepository.class);
        processingAuditAccessor = new DefaultProcessingAuditAccessor(
            auditEntryRepository,
            auditNotificationRepository,
            auditFailedEntryRepository,
            auditFailedNotificationRepository
        );
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

        AuditFailedHandler handler = new AuditFailedHandler(processingAuditAccessor);
        processingAuditAccessor.createOrUpdatePendingAuditEntryForJob(jobId, notificationIds);
        AuditFailedEvent event = new AuditFailedEvent(jobId, notificationIds, errorMessage, stackTrace);

        handler.handle(event);

        for (Long notificationId : notificationIds) {
            Optional<AuditEntryEntity> entry = auditEntryRepository.findMatchingAudit(notificationId, jobId);
            assertTrue(entry.isPresent());
            AuditEntryEntity entity = entry.get();
            assertEquals(AuditEntryStatus.FAILURE.name(), entity.getStatus());
            assertNotNull(entity.getTimeCreated());
            assertTrue(entity.getTimeLastSent().isAfter(entity.getTimeCreated()));
            assertEquals(errorMessage, entity.getErrorMessage());
            assertEquals(stackTrace, entity.getErrorStackTrace());
        }
    }

    @Test
    void handleEventAuditEntryMissingTest() {
        UUID jobId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);
        String errorMessage = "Error message";
        String stackTrace = "Stack trace goes here";

        AuditFailedHandler handler = new AuditFailedHandler(processingAuditAccessor);
        AuditFailedEvent event = new AuditFailedEvent(jobId, notificationIds, errorMessage, stackTrace);

        handler.handle(event);

        for (Long notificationId : notificationIds) {
            Optional<AuditEntryEntity> entry = auditEntryRepository.findMatchingAudit(notificationId, jobId);
            assertTrue(entry.isEmpty());
        }
    }
}

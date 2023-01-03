package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.distribution.execution.ExecutingJob;
import com.synopsys.integration.alert.api.distribution.execution.ExecutingJobManager;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditEntryRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditNotificationRepository;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.database.api.DefaultProcessingAuditAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelation;
import com.synopsys.integration.alert.database.audit.AuditNotificationRelationPK;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

class AuditSuccessHandlerTest {
    private ProcessingAuditAccessor processingAuditAccessor;
    private AuditEntryRepository auditEntryRepository;
    private AuditNotificationRepository auditNotificationRepository;
    private ExecutingJobManager executingJobManager;
    private AtomicLong idContainer = new AtomicLong(0L);

    @BeforeEach
    public void init() {
        auditNotificationRepository = new MockAuditNotificationRepository(this::generateRelationKey);
        auditEntryRepository = new MockAuditEntryRepository(this::generateEntityKey, auditNotificationRepository);
        processingAuditAccessor = new DefaultProcessingAuditAccessor(auditEntryRepository, auditNotificationRepository);
        executingJobManager = new ExecutingJobManager();
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
        ExecutingJob executingJob = executingJobManager.startJob(jobId);
        UUID jobExecutionId = executingJob.getExecutionId();
        AuditSuccessHandler handler = new AuditSuccessHandler(processingAuditAccessor, executingJobManager);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, Set.of());
        handler.handle(event);
        executingJob = executingJobManager.getExecutingJob(jobExecutionId).orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.SUCCESS, executingJob.getStatus());
    }

    @Test
    void handleEventAuditMissingTest() {
        UUID jobExecutionId = UUID.randomUUID();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        AuditSuccessHandler handler = new AuditSuccessHandler(processingAuditAccessor, executingJobManager);
        AuditSuccessEvent event = new AuditSuccessEvent(jobExecutionId, notificationIds);
        handler.handle(event);
        Optional<ExecutingJob> executingJob = executingJobManager.getExecutingJob(jobExecutionId);
        assertTrue(executingJob.isEmpty());
    }
}

package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.google.gson.Gson;
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

class AuditSuccessEventListenerTest {
    private final Gson gson = new Gson();
    private final TaskExecutor taskExecutor = new SyncTaskExecutor();
    private ProcessingAuditAccessor processingAuditAccessor;
    private AuditEntryRepository auditEntryRepository;

    private ExecutingJobManager executingJobManager;
    private final AtomicLong idContainer = new AtomicLong(0L);
    private AuditSuccessHandler handler;

    @BeforeEach
    public void init() {
        AuditNotificationRepository auditNotificationRepository = new MockAuditNotificationRepository(this::generateRelationKey);
        auditEntryRepository = new MockAuditEntryRepository(this::generateEntityKey, auditNotificationRepository);
        processingAuditAccessor = new DefaultProcessingAuditAccessor(auditEntryRepository, auditNotificationRepository);
        executingJobManager = new ExecutingJobManager();
        handler = new AuditSuccessHandler(processingAuditAccessor, executingJobManager);
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
    void onMessageTest() {
        UUID jobId = UUID.randomUUID();
        ExecutingJob executingJob = executingJobManager.startJob(jobId);
        UUID executingJobId = executingJob.getExecutionId();
        Set<Long> notificationIds = Set.of(1L, 2L, 3L);

        AuditSuccessEventListener listener = new AuditSuccessEventListener(gson, taskExecutor, handler);
        processingAuditAccessor.createOrUpdatePendingAuditEntryForJob(jobId, notificationIds);
        AuditSuccessEvent event = new AuditSuccessEvent(executingJobId, notificationIds);
        Message message = new Message(gson.toJson(event).getBytes());
        listener.onMessage(message);

        executingJob = executingJobManager.getExecutingJob(executingJobId).orElseThrow(() -> new AssertionError("Executing Job cannot be missing from the test."));
        assertEquals(AuditEntryStatus.SUCCESS, executingJob.getStatus());
    }
}

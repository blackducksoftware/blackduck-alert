package com.synopsys.integration.alert.api.distribution.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.distribution.mock.MockAuditFailedEntryRepository;
import com.synopsys.integration.alert.api.distribution.mock.MockAuditFailedNotificationRepository;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.NotificationAccessor;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultProcessingFailedAccessor;
import com.synopsys.integration.alert.database.audit.AuditFailedEntity;
import com.synopsys.integration.alert.database.audit.AuditFailedNotificationEntity;

class FailedAuditPurgeTaskTest {

    @Test
    void cronExpressionTest() {
        FailedAuditPurgeTask task = new FailedAuditPurgeTask(null, null, null);
        assertEquals(FailedAuditPurgeTask.CRON_EXPRESSION, task.scheduleCronExpression());
    }

    @Test
    void purgeDataTest() {
        MockAuditFailedEntryRepository auditFailedEntryRepository = new MockAuditFailedEntryRepository(AuditFailedEntity::getId);
        MockAuditFailedNotificationRepository auditFailedNotificationRepository = new MockAuditFailedNotificationRepository(AuditFailedNotificationEntity::getNotificationId);
        NotificationAccessor notificationAccessor = Mockito.mock(NotificationAccessor.class);
        JobAccessor jobAccessor = Mockito.mock(JobAccessor.class);
        DefaultProcessingFailedAccessor processingFailedAccessor = new DefaultProcessingFailedAccessor(
            auditFailedEntryRepository,
            auditFailedNotificationRepository,
            notificationAccessor,
            jobAccessor
        );

        UUID expectedId = UUID.randomUUID();
        UUID firstIdToBeRemoved = UUID.randomUUID();
        UUID secondIdToBeRemoved = UUID.randomUUID();
        Long expectedNotificationId = 1L;
        auditFailedEntryRepository.save(new AuditFailedEntity(
            expectedId,
            DateUtils.createCurrentDateTimestamp(),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            expectedNotificationId
        ));
        auditFailedEntryRepository.save(new AuditFailedEntity(
            firstIdToBeRemoved,
            DateUtils.createCurrentDateTimestamp().minusDays(12),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            2L
        ));

        auditFailedEntryRepository.save(new AuditFailedEntity(
            secondIdToBeRemoved,
            DateUtils.createCurrentDateTimestamp().minusDays(25),
            "jobName",
            "providerKey",
            "providerName",
            "channelName",
            "notificationType",
            "errorMessage",
            3L
        ));

        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(1L, "notification 1 content"));
        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(2L, "notification 2 content"));
        auditFailedNotificationRepository.save(new AuditFailedNotificationEntity(3L, "notification 3 content"));

        FailedAuditPurgeTask task = new FailedAuditPurgeTask(null, null, processingFailedAccessor);
        task.runTask();
        assertEquals(1, auditFailedEntryRepository.count());
        assertEquals(1, auditFailedNotificationRepository.count());
        assertTrue(auditFailedEntryRepository.existsById(expectedId));
        assertFalse(auditFailedEntryRepository.existsById(firstIdToBeRemoved));
        assertFalse(auditFailedEntryRepository.existsById(secondIdToBeRemoved));
        assertTrue(auditFailedNotificationRepository.existsById(expectedNotificationId));
        assertFalse(auditFailedNotificationRepository.existsById(2L));
        assertFalse(auditFailedNotificationRepository.existsById(3L));
    }
}

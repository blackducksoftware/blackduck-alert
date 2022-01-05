package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryNotificationView;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;
import com.synopsys.integration.alert.database.audit.AuditNotificationRepository;

public class DefaultProcessingAuditAccessorTest {
    private static final Random RANDOM = new Random();

    @Test
    public void createOrUpdatePendingAuditEntryForJobTest() {
        UUID testJobId = UUID.randomUUID();
        Set<Long> testNotificationIds = Set.of(1L, 2L, 10L);

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findByJobIdAndNotificationIds(Mockito.eq(testJobId), Mockito.eq(testNotificationIds))).thenReturn(List.of());
        Mockito.when(auditEntryRepository.save(Mockito.any())).then(invocation -> {
            AuditEntryEntity auditEntry = invocation.getArgument(0);
            auditEntry.setId(RANDOM.nextLong());
            return auditEntry;
        });

        AuditNotificationRepository auditNotificationRepository = Mockito.mock(AuditNotificationRepository.class);
        Mockito.when(auditNotificationRepository.saveAll(Mockito.anyCollection())).thenReturn(List.of());

        DefaultProcessingAuditAccessor processingAuditAccessor = new DefaultProcessingAuditAccessor(auditEntryRepository, auditNotificationRepository);
        processingAuditAccessor.createOrUpdatePendingAuditEntryForJob(testJobId, testNotificationIds);

        Mockito.verify(auditEntryRepository, Mockito.times(testNotificationIds.size())).save(Mockito.any());
    }

    @Test
    public void setAuditEntrySuccessTest() {
        setAuditEntryStatusTest(
            AuditEntryStatus.SUCCESS,
            ProcessingAuditAccessor::setAuditEntrySuccess
        );
    }

    @Test
    public void setAuditEntryFailureTest() {
        String testErrorMessage = "Uh oh, an error occurred!";
        String testExceptionMessage = "Something bad happened. Yikes...";
        Throwable testThrowable = new AlertException(testExceptionMessage);
        AuditEntryEntity testResultEntry = setAuditEntryStatusTest(
            AuditEntryStatus.FAILURE,
            (accessor, jobId, notificationIds) -> accessor.setAuditEntryFailure(jobId, notificationIds, testErrorMessage, testThrowable)
        );
        assertEquals(testErrorMessage, testResultEntry.getErrorMessage());
        assertNotNull(testResultEntry.getErrorStackTrace(), "Expected the audit entry to contain an error stack trace");
        assertTrue(testResultEntry.getErrorStackTrace().contains(testExceptionMessage), "Expected the error stack trace to contain a specific message, but that message was missing");
    }

    private AuditEntryEntity setAuditEntryStatusTest(AuditEntryStatus expectedStatus, AuditAccessorStatusSetter statusSetter) {
        UUID testJobId = UUID.randomUUID();
        Long testNotificationId = 99L;
        Set<Long> testNotificationIds = Set.of(testNotificationId);
        AuditEntryNotificationView testView = new AuditEntryNotificationView(0L, testJobId, testNotificationId, null, null, null, null, null);

        AtomicReference<AuditEntryEntity> savedEntry = new AtomicReference<>();

        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        Mockito.when(auditEntryRepository.findByJobIdAndNotificationIds(testJobId, testNotificationIds)).thenReturn(List.of(testView));
        Mockito.when(auditEntryRepository.saveAll(Mockito.anyList())).then(invocation -> {
            List<AuditEntryEntity> savedEntries = invocation.getArgument(0);
            if (!savedEntries.isEmpty()) {
                savedEntry.set(savedEntries.get(0));
            }
            return List.of();
        });

        DefaultProcessingAuditAccessor processingAuditAccessor = new DefaultProcessingAuditAccessor(auditEntryRepository, null);
        statusSetter.setStatus(processingAuditAccessor, testJobId, testNotificationIds);

        AuditEntryEntity auditEntryEntity = savedEntry.get();
        assertNotNull(auditEntryEntity, "Expected an audit entry to have been saved");
        assertEquals(testView.getId(), auditEntryEntity.getId());
        assertEquals(testView.getJobId(), auditEntryEntity.getCommonConfigId());
        assertEquals(expectedStatus.name(), auditEntryEntity.getStatus());
        assertNotNull(auditEntryEntity.getTimeLastSent(), "Expected time last sent to be set");

        return auditEntryEntity;
    }

    @FunctionalInterface
    private interface AuditAccessorStatusSetter {
        void setStatus(ProcessingAuditAccessor accessor, UUID jobId, Set<Long> notificationIds);

    }

}

package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
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

        DefaultProcessingAuditAccessor auditUtility = new DefaultProcessingAuditAccessor(auditEntryRepository, auditNotificationRepository);
        auditUtility.createOrUpdatePendingAuditEntryForJob(testJobId, testNotificationIds);

        Mockito.verify(auditEntryRepository, Mockito.times(testNotificationIds.size())).save(Mockito.any());
    }

    // OLD:

    @Test
    public void setAuditEntrySuccessCatchExceptionTest() {
        DefaultProcessingAuditAccessor auditUtility = new DefaultProcessingAuditAccessor(null, null);
        auditUtility.setAuditEntrySuccess(List.of(1L));
    }

    @Test
    public void setAuditEntrySuccessTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        DefaultProcessingAuditAccessor auditUtility = new DefaultProcessingAuditAccessor(auditEntryRepository, null);

        AuditEntryEntity entity = new AuditEntryEntity(UUID.randomUUID(), DateUtils.createCurrentDateTimestamp().minusSeconds(1), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.SUCCESS.toString(), null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntrySuccess(List.of());
        auditUtility.setAuditEntrySuccess(List.of(entity.getId()));
        assertEquals(AuditEntryStatus.SUCCESS.toString(), entity.getStatus());
    }

    @Test
    public void setAuditEntryFailureCatchExceptionTest() {
        DefaultProcessingAuditAccessor auditUtility = new DefaultProcessingAuditAccessor(null, null);
        auditUtility.setAuditEntryFailure(List.of(1L), null, null);
    }

    @Test
    public void setAuditEntryFailureTest() {
        AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        DefaultProcessingAuditAccessor auditUtility = new DefaultProcessingAuditAccessor(auditEntryRepository, null);
        AuditEntryEntity entity = new AuditEntryEntity(UUID.randomUUID(), DateUtils.createCurrentDateTimestamp().minusSeconds(1), DateUtils.createCurrentDateTimestamp(), AuditEntryStatus.FAILURE.toString(), null, null);
        entity.setId(1L);
        Mockito.when(auditEntryRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(entity));
        Mockito.when(auditEntryRepository.save(entity)).thenReturn(entity);

        auditUtility.setAuditEntryFailure(List.of(), null, null);
        auditUtility.setAuditEntryFailure(List.of(entity.getId()), "error", new Exception());
        assertEquals(AuditEntryStatus.FAILURE.toString(), entity.getStatus());
        assertEquals("error", entity.getErrorMessage());
    }

}

package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.audit.AuditEntryEntity;
import com.synopsys.integration.alert.database.audit.AuditEntryRepository;

public class DefaultProcessingAuditAccessorTest {
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

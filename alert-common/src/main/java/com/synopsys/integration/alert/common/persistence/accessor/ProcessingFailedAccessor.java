package com.synopsys.integration.alert.common.persistence.accessor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;

public interface ProcessingFailedAccessor {

    AuditEntryPageModel getPageOfAuditEntries(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder);

    void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage);

    void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage, String stackTrace);

    void deleteAuditEntriesBefore(OffsetDateTime expirationDate);
}

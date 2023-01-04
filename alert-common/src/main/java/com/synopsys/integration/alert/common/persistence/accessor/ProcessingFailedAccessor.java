package com.synopsys.integration.alert.common.persistence.accessor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public interface ProcessingFailedAccessor {

    void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage);

    void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage, String stackTrace);
}

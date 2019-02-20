package com.synopsys.integration.alert.common.data;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import com.synopsys.integration.alert.common.model.AggregateMessageContent;

public interface AuditUtility {
    Map<Long, Long> createAuditEntry(final Map<Long, Long> existingNotificationIdToAuditId, final UUID jobId, final AggregateMessageContent content);

    void setAuditEntrySuccess(final Collection<Long> auditEntryIds);

    void setAuditEntryFailure(final Collection<Long> auditEntryIds, final String errorMessage, final Throwable t);
}

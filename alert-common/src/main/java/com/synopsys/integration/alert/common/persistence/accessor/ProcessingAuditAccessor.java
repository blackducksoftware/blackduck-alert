/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface ProcessingAuditAccessor {
    Long findOrCreatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds);

    void setAuditEntrySuccess(Collection<Long> auditEntryIds);

    void setAuditEntryFailure(Collection<Long> auditEntryIds, String errorMessage, Throwable t);

}

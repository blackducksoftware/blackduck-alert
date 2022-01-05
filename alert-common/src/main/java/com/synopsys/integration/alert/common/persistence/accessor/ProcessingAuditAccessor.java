/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public interface ProcessingAuditAccessor {
    void createOrUpdatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds);

    void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds);

    void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable Throwable exception);

}

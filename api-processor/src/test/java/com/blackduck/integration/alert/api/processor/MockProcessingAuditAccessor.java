/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;

public class MockProcessingAuditAccessor implements ProcessingAuditAccessor {
    private Map<UUID, Set<Long>> auditEntries = new HashMap<>();

    @Override
    public void createOrUpdatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds) {
        auditEntries.put(jobId, notificationIds);
    }

    @Override
    public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds) {
        //Do Nothing
    }

    @Override
    public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds, OffsetDateTime successTimestamp) {
        //Do Nothing
    }

    @Override
    public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable Throwable exception) {
        //Do Nothing
    }

    @Override
    public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable String stackTrace) {
        //Do Nothing
    }

    @Override
    public void setAuditEntryFailure(
        UUID jobId,
        Set<Long> notificationIds,
        OffsetDateTime failureTimestamp,
        String errorMessage,
        @Nullable String stackTrace
    ) {
        //Do Nothing
    }

    public Map<UUID, Set<Long>> getAuditEntries() {
        return auditEntries;
    }

    public Set<Long> getNotificationIds(UUID jobId) {
        return auditEntries.get(jobId);
    }
}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.slack.distribution.mock;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.common.persistence.accessor.ProcessingAuditAccessor;

public class MockProcessingAuditAccessor implements ProcessingAuditAccessor {
    //A Set<Long> is used because an event success or failure will mark all notifications in the event as a success or failure.
    //Multiple notifications can be sent as part of a job.
    private Map<UUID, Set<Long>> successes = new HashMap<>();
    private Map<UUID, Set<Long>> failures = new HashMap<>();

    @Override
    public void createOrUpdatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds) {
        //do nothing
    }

    @Override
    public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds) {
        successes.put(jobId, notificationIds);
    }

    @Override
    public void setAuditEntrySuccess(UUID jobId, Set<Long> notificationIds, OffsetDateTime successTimestamp) {
        successes.put(jobId, notificationIds);
    }

    @Override
    public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable Throwable exception) {
        failures.put(jobId, notificationIds);
    }

    @Override
    public void setAuditEntryFailure(UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable String stackTrace) {
        failures.put(jobId, notificationIds);
    }

    @Override
    public void setAuditEntryFailure(
        UUID jobId,
        Set<Long> notificationIds,
        OffsetDateTime failureTimestamp,
        String errorMessage,
        @Nullable String stackTrace
    ) {
        failures.put(jobId, notificationIds);
    }

    public Map<UUID, Set<Long>> getSuccesses() {
        return successes;
    }

    public Map<UUID, Set<Long>> getFailures() {
        return failures;
    }

    public Set<Long> getSuccessfulIds() {
        return getIds(successes);
    }

    public Set<Long> getFailureIds() {
        return getIds(failures);
    }

    private Set<Long> getIds(Map<UUID, Set<Long>> results) {
        return results.values()
                   .stream()
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }
}

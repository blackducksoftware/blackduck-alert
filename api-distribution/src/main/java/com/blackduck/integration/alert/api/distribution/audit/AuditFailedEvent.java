/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.audit;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class AuditFailedEvent extends AuditEvent {
    private static final long serialVersionUID = 7700792205184047256L;
    public static final String DEFAULT_DESTINATION_NAME = "audit_failed_event";

    private final String errorMessage;
    private final String stackTrace;

    public AuditFailedEvent(UUID jobExecutionId, UUID jobConfigId, Set<Long> notificationIds, String errorMessage, @Nullable String stackTrace) {
        super(DEFAULT_DESTINATION_NAME, jobExecutionId, jobConfigId, notificationIds);
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Optional<String> getStackTrace() {
        return Optional.ofNullable(stackTrace);
    }
}

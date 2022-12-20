package com.synopsys.integration.alert.api.distribution.audit;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

public class AuditFailedEvent extends AuditEvent {
    private static final long serialVersionUID = 7700792205184047256L;
    public static final String DEFAULT_DESTINATION_NAME = "audit_failed_event";

    private final String errorMessage;
    private final String stackTrace;

    public AuditFailedEvent(UUID jobExecutionId, Set<Long> notificationIds, String errorMessage, @Nullable String stackTrace) {
        super(DEFAULT_DESTINATION_NAME, jobExecutionId, notificationIds);
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

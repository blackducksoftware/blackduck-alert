package com.synopsys.integration.alert.api.channel.issue.event.distribution;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.event.AlertEvent;

public class AuditFailedEvent extends AlertEvent {
    private final UUID jobId;
    private final Set<Long> notificationIds;
    private final String errorMessage;
    private final String stackTrace;

    public AuditFailedEvent(String destination, UUID jobId, Set<Long> notificationIds, String errorMessage, @Nullable String stackTrace) {
        super(destination);
        this.jobId = jobId;
        this.notificationIds = notificationIds;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Set<Long> getNotificationIds() {
        return notificationIds;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Optional<String> getStackTrace() {
        return Optional.ofNullable(stackTrace);
    }
}

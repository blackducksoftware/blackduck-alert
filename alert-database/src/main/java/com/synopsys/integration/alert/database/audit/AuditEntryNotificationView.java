/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.audit;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AuditEntryNotificationView implements Serializable {
    private Long id;
    private UUID jobId;
    private Long notificationId;
    private OffsetDateTime timeCreated;
    private OffsetDateTime timeLastSent;
    private String status;
    private String errorMessage;
    private String errorStackTrace;

    public AuditEntryNotificationView() {
        // For Serialization
    }

    public AuditEntryNotificationView(Long id, UUID jobId, Long notificationId, OffsetDateTime timeCreated, OffsetDateTime timeLastSent, String status, String errorMessage, String errorStackTrace) {
        this.id = id;
        this.jobId = jobId;
        this.notificationId = notificationId;
        this.timeCreated = timeCreated;
        this.timeLastSent = timeLastSent;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorStackTrace = errorStackTrace;
    }

    public Long getId() {
        return id;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public OffsetDateTime getTimeCreated() {
        return timeCreated;
    }

    public OffsetDateTime getTimeLastSent() {
        return timeLastSent;
    }

    public String getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

}

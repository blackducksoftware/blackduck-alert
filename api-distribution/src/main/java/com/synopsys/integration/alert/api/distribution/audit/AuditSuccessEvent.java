package com.synopsys.integration.alert.api.distribution.audit;

import java.util.Set;
import java.util.UUID;

public class AuditSuccessEvent extends AuditEvent {
    private static final long serialVersionUID = -2708967824272053739L;

    public static final String DEFAULT_DESTINATION_NAME = "audit_success_event";

    public AuditSuccessEvent(UUID jobExecutionId, Set<Long> notificationIds) {
        super(DEFAULT_DESTINATION_NAME, jobExecutionId, notificationIds);
    }
}

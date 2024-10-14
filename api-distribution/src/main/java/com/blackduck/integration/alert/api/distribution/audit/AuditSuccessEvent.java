/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.distribution.audit;

import java.util.Set;
import java.util.UUID;

public class AuditSuccessEvent extends AuditEvent {
    private static final long serialVersionUID = -2708967824272053739L;

    public static final String DEFAULT_DESTINATION_NAME = "audit_success_event";

    public AuditSuccessEvent(UUID jobExecutionId, UUID jobConfigId, Set<Long> notificationIds) {
        super(DEFAULT_DESTINATION_NAME, jobExecutionId, jobConfigId, notificationIds);
    }
}

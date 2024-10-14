/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;

public interface ProcessingFailedAccessor {

    AuditEntryPageModel getPageOfAuditEntries(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder);

    void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage);

    void setAuditFailure(UUID jobId, Set<Long> notificationIds, OffsetDateTime occurrence, String errorMessage, String stackTrace);

    void deleteAuditEntriesBefore(OffsetDateTime expirationDate);

    void deleteAuditsWithNotificationId(Long notificationId);

    void deleteAuditsWithJobIdAndNotificationId(UUID jobId, Long notificationId);
}

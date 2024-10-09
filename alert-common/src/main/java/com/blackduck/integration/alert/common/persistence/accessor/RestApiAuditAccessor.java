/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import com.blackduck.integration.alert.common.persistence.model.AuditEntryModel;
import com.blackduck.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.blackduck.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.blackduck.integration.alert.common.rest.model.AlertNotificationModel;

public interface RestApiAuditAccessor {
    Optional<Long> findMatchingAuditId(Long notificationId, UUID commonDistributionId);

    Optional<AuditJobStatusModel> findFirstByJobId(UUID jobId);

    List<AuditJobStatusModel> findByJobIds(Collection<UUID> jobIds);

    AuditEntryPageModel getPageOfAuditEntries(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder, boolean onlyShowSentNotifications,
        Function<AlertNotificationModel, AuditEntryModel> notificationToAuditEntryConverter);

    AuditEntryModel convertToAuditEntryModelFromNotification(AlertNotificationModel notificationContentEntry);

}

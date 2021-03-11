/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryModel;
import com.synopsys.integration.alert.common.persistence.model.AuditEntryPageModel;
import com.synopsys.integration.alert.common.persistence.model.AuditJobStatusModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;

public interface AuditAccessor {
    Optional<Long> findMatchingAuditId(Long notificationId, UUID commonDistributionId);

    Optional<AuditJobStatusModel> findFirstByJobId(UUID jobId);

    List<AuditJobStatusModel> findByJobIds(Collection<UUID> jobIds);

    AuditEntryPageModel getPageOfAuditEntries(Integer pageNumber, Integer pageSize, String searchTerm, String sortField, String sortOrder, boolean onlyShowSentNotifications,
        Function<AlertNotificationModel, AuditEntryModel> notificationToAuditEntryConverter);

    Long findOrCreatePendingAuditEntryForJob(UUID jobId, Set<Long> notificationIds);

    Map<Long, Long> createAuditEntry(Map<Long, Long> existingNotificationIdToAuditId, UUID jobId, MessageContentGroup content);

    void setAuditEntrySuccess(Collection<Long> auditEntryIds);

    void setAuditEntryFailure(Collection<Long> auditEntryIds, String errorMessage, Throwable t);

    AuditEntryModel convertToAuditEntryModelFromNotification(AlertNotificationModel notificationContentEntry);

}

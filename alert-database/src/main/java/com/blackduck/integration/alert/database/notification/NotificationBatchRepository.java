/*
 * blackduck-alert
 *
 * Copyright (c) 2026 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.notification;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationBatchRepository extends JpaRepository<NotificationBatchEntity, NotificationBatchPK> {

    @Query(value = "SELECT DISTINCT batchEntity.batchId FROM NotificationEntity notification"
        + " INNER JOIN notification.notificationBatches batchEntity on notification.id = batchEntity.notificationId"
        + " WHERE batchEntity.providerId = :providerId AND notification.processed = false")
    Page<UUID> findUniqueBatchIdsForProviderWhereNotificationsNotProcessed(@Param("providerId") Long providerId, Pageable pageable);
}

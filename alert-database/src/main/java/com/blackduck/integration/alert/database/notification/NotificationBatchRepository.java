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

public interface NotificationBatchRepository extends JpaRepository<NotificationBatchEntity, NotificationBatchPK> {

    @Query(value =
            "SELECT DISTINCT rnb.batch_id"
                + " FROM alert.raw_notification_batch AS rnb"
                + " JOIN alert.raw_notification_content AS rnc"
                + " ON rnb.notification_id = rnc.id"
                + " WHERE rnb.provider_id = :provider_id"
                + " AND rnc.processed = false"
                + " ORDER BY rnb.notification_id",
        countQuery =
            "SELECT DISTINCT COUNT(rnb.batch_id)"
                + " FROM alert.raw_notification_batch AS rnb"
                + " JOIN alert.raw_notification_content AS rnc"
                + " ON rnb.notification_id = rnc.id"
                + " WHERE rnb.provider_id = :provider_id"
                + " AND rnc.processed = false",
        nativeQuery = true)
    Page<UUID> findUniqueBatchIdsForProviderWhereNotificationsNotProcessed(Long providerId, Pageable pageable);

}

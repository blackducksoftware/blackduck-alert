/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.notification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationContentRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("SELECT entity FROM NotificationEntity entity WHERE entity.createdAt BETWEEN ?1 AND ?2 ORDER BY created_at, provider_creation_time asc")
    Page<NotificationEntity> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);

    @Query("SELECT entity FROM NotificationEntity entity WHERE entity.createdAt < ?1 ORDER BY created_at, provider_creation_time asc")
    List<NotificationEntity> findByCreatedAtBefore(OffsetDateTime date);

    @Query(value = "SELECT entity FROM NotificationEntity entity WHERE entity.id IN (SELECT notificationId FROM entity.auditNotificationRelations WHERE entity.id = notificationId)")
    Page<NotificationEntity> findAllSentNotifications(Pageable pageable);

    @Query(value = "SELECT DISTINCT notificationRow "
        + "FROM NotificationEntity notificationRow "
        + "LEFT JOIN notificationRow.auditNotificationRelations relation ON notificationRow.id = relation.notificationId "
        + "LEFT JOIN relation.auditEntryEntity auditEntry ON auditEntry.id = relation.auditEntryId "
        + "LEFT JOIN DistributionJobEntity jobEntity ON auditEntry.commonConfigId = jobEntity.jobId "
        + "WHERE LOWER(notificationRow.provider) LIKE %:searchTerm% OR "
        + "LOWER(notificationRow.notificationType) LIKE %:searchTerm% OR "
        + "LOWER(notificationRow.content) LIKE %:searchTerm% OR "
        + "COALESCE(to_char(notificationRow.createdAt, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm% OR "
        + "COALESCE(to_char(auditEntry.timeLastSent, 'MM/DD/YYYY, HH24:MI:SS'), '') LIKE %:searchTerm% OR "
        + "LOWER(auditEntry.status) LIKE %:searchTerm% OR "
        + "LOWER(jobEntity.name) LIKE %:searchTerm% OR "
        + "LOWER(jobEntity.channelDescriptorName) LIKE %:searchTerm%"
    )
    Page<NotificationEntity> findMatchingNotification(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query(value = "SELECT DISTINCT notificationRow "
                       + "FROM NotificationEntity notificationRow "
                       + "LEFT JOIN notificationRow.auditNotificationRelations relation ON notificationRow.id = relation.notificationId "
                       + "LEFT JOIN relation.auditEntryEntity auditEntry ON auditEntry.id = relation.auditEntryId "
                       + "LEFT JOIN DistributionJobEntity jobEntity ON auditEntry.commonConfigId = jobEntity.jobId "
                       + "WHERE notificationRow.id IN (SELECT notificationId FROM notificationRow.auditNotificationRelations WHERE notificationRow.id = notificationId) AND "
                       + "("
                       + "LOWER(notificationRow.provider) LIKE %:searchTerm% OR "
                       + "LOWER(notificationRow.notificationType) LIKE %:searchTerm% OR "
                       + "LOWER(notificationRow.content) LIKE %:searchTerm% OR "
                       + "COALESCE(to_char(notificationRow.createdAt, 'MM-DD-YYYY HH24:MI:SS'), '') LIKE %:searchTerm% OR "
                       + "COALESCE(to_char(auditEntry.timeLastSent, 'MM-DD-YYYY HH24:MI:SS'), '') LIKE %:searchTerm% OR "
                       + "LOWER(auditEntry.status) LIKE %:searchTerm% OR "
                       + "LOWER(jobEntity.name) LIKE %:searchTerm% OR "
                       + "LOWER(jobEntity.channelDescriptorName) LIKE %:searchTerm%"
                       + ")"
    )
    Page<NotificationEntity> findMatchingSentNotification(@Param("searchTerm") String searchTerm, Pageable pageable);

    Page<NotificationEntity> findByProcessedFalseOrderByProviderCreationTimeAsc(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationEntity entity "
               + "SET entity.processed = true "
               + "WHERE entity.id IN :notificationIds"
    )
    void setProcessedByIds(@Param("notificationIds") Set<Long> notificationIds);

    @Query("DELETE FROM NotificationEntity notification"
               + " WHERE notification.createdAt < :date"
    )
    @Modifying
    int bulkDeleteCreatedAtBefore(@Param("date") OffsetDateTime date);

    boolean existsByProcessedFalse();
}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.notification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationContentRepository extends JpaRepository<NotificationEntity, Long> {
    @Query("SELECT entity FROM NotificationEntity entity WHERE entity.createdAt BETWEEN ?1 AND ?2 ORDER BY entity.createdAt, entity.providerCreationTime asc")
    Page<NotificationEntity> findByCreatedAtBetween(OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);

    @Query("SELECT entity FROM NotificationEntity entity WHERE entity.createdAt < ?1 ORDER BY entity.createdAt, entity.providerCreationTime asc")
    List<NotificationEntity> findByCreatedAtBefore(OffsetDateTime date);

    @Query(value = "SELECT entity FROM NotificationEntity entity WHERE entity.id IN (SELECT relation.notificationId FROM entity.auditNotificationRelations relation WHERE entity.id = relation.notificationId)")
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
        + "WHERE notificationRow.id IN (SELECT relation.notificationId FROM notificationRow.auditNotificationRelations relation WHERE notificationRow.id = relation.notificationId) AND "
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

    Page<NotificationEntity> findByProviderConfigIdAndProcessedFalseOrderByProviderCreationTimeAsc(long providerConfigId, Pageable pageable);

    List<NotificationEntity> findAllByIdInOrderByProviderCreationTimeAsc(List<Long> notificationIds);

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

    boolean existsByProviderConfigIdAndProcessedFalse(long providerConfigId);

    long countByProcessed(boolean processed);

    boolean existsByContentId(String contentId);

    long countByProviderConfigIdAndNotificationType(long providerConfigId, String notificationType);

    @Query(value ="SELECT entity FROM NotificationEntity entity"
        + " INNER JOIN NotificationBatchEntity batch ON entity.id = batch.notificationId"
        + " WHERE batch.batchId = :batchId"
        + " AND entity.providerConfigId = :providerId"
        + " AND entity.mappingToJobs = false"
        + " AND entity.processed = false"
        + " ORDER BY entity.providerCreationTime ASC")
    Page<NotificationEntity> findNotMappedAndNotProcessedNotifications(@Param("providerId") long providerConfigId, @Param("batchId") UUID batchId, Pageable pageable);

    boolean existsByProviderConfigIdAndMappingToJobsFalse(@Param("providerId") long providerConfigId, @Param("batchId")  UUID batchId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationEntity entity "
            + "SET entity.mappingToJobs = true "
            + "WHERE entity.id IN :notificationIds"
    )
    void setMappingToJobsByIds(@Param("notificationIds") Set<Long> notificationIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE NotificationEntity entity "
            + "SET entity.mappingToJobs = false "
            + "WHERE entity.providerConfigId = :providerConfigId "
            + "AND entity.processed = false"
    )
    void setMappingToJobsFalseWhenProcessedFalse(@Param("providerConfigId") long providerConfigId);
}

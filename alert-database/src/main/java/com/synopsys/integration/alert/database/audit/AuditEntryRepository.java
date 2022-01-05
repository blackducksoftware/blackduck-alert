/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.audit;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEntryRepository extends JpaRepository<AuditEntryEntity, Long> {
    @Query(
        "SELECT new com.synopsys.integration.alert.database.audit.AuditEntryNotificationView(audit.id, audit.commonConfigId, auditNotification.notificationId, audit.timeCreated, audit.timeLastSent, audit.status, audit.errorMessage, audit.errorStackTrace)"
            + " FROM AuditEntryEntity audit"
            + " LEFT JOIN audit.auditNotificationRelations auditNotification"
            + " WHERE audit.commonConfigId = :jobId"
            + " AND auditNotification.notificationId IN :notificationIds"
            + " ORDER BY audit.timeLastSent DESC"
    )
    List<AuditEntryNotificationView> findByJobIdAndNotificationIds(@Param("jobId") UUID jobId, @Param("notificationIds") Collection<Long> notificationIds);

    Optional<AuditEntryEntity> findFirstByCommonConfigIdOrderByTimeLastSentDesc(UUID commonConfigId);

    @Query("SELECT entity FROM AuditEntryEntity entity"
               + " INNER JOIN entity.auditNotificationRelations relation ON entity.id = relation.auditEntryId"
               + " WHERE entity.commonConfigId = ?2"
               + " AND relation.notificationContent.id = ?1"
    )
    Optional<AuditEntryEntity> findMatchingAudit(Long notificationId, UUID commonConfigId);

    @Query("DELETE FROM AuditEntryEntity audit"
               + " WHERE audit.id NOT IN (SELECT relation.auditEntryId FROM com.synopsys.integration.alert.database.audit.AuditNotificationRelation relation)"
    )
    @Modifying
    void bulkDeleteOrphanedEntries();

}

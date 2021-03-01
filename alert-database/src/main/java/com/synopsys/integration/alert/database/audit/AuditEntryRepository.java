/*
 * alert-database
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.audit;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuditEntryRepository extends JpaRepository<AuditEntryEntity, Long> {
    Optional<AuditEntryEntity> findFirstByCommonConfigIdOrderByTimeLastSentDesc(UUID commonConfigId);

    @Query(value = "SELECT entity FROM AuditEntryEntity entity INNER JOIN entity.auditNotificationRelations relation ON entity.id = relation.auditEntryId WHERE entity.commonConfigId = ?2 AND relation.notificationContent.id = ?1")
    Optional<AuditEntryEntity> findMatchingAudit(Long notificationId, UUID commonConfigId);

}

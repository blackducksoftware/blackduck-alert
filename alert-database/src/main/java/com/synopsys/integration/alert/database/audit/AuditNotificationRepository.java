/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditNotificationRepository extends JpaRepository<AuditNotificationRelation, AuditNotificationRelationPK> {
    List<AuditNotificationRelation> findByAuditEntryId(Long auditEntryId);

    List<AuditNotificationRelation> findByNotificationId(Long notificationId);
}

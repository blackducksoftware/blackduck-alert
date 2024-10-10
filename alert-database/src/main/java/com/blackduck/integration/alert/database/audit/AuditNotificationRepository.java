/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditNotificationRepository extends JpaRepository<AuditNotificationRelation, AuditNotificationRelationPK> {
    List<AuditNotificationRelation> findByAuditEntryId(Long auditEntryId);

    List<AuditNotificationRelation> findByNotificationId(Long notificationId);

    List<AuditNotificationRelation> findAllByNotificationIdIn(List<Long> notificationIds);
}

package com.blackduck.integration.alert.database.audit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditNotificationRepository extends JpaRepository<AuditNotificationRelation, AuditNotificationRelationPK> {
    List<AuditNotificationRelation> findByAuditEntryId(Long auditEntryId);

    List<AuditNotificationRelation> findByNotificationId(Long notificationId);

    List<AuditNotificationRelation> findAllByNotificationIdIn(List<Long> notificationIds);
}

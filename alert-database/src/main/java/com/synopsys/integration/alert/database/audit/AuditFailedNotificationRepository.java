package com.synopsys.integration.alert.database.audit;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditFailedNotificationRepository extends JpaRepository<AuditFailedNotificationRelation, AuditFailedNotificationRelationPK> {

    List<AuditFailedNotificationRelation> findAuditFailedNotificationRelationsByNotificationId(Long notificationId);

    List<AuditFailedNotificationRelation> findAuditFailedNotificationRelationsByFailedAuditEntryId(UUID failedAuditEntryId);
}

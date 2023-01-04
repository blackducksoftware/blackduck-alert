package com.synopsys.integration.alert.database.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditFailedNotificationRepository extends JpaRepository<AuditFailedNotificationRelation, AuditFailedNotificationRelationPK> {
}

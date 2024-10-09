package com.blackduck.integration.alert.database.audit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditFailedNotificationRepository extends JpaRepository<AuditFailedNotificationEntity, Long> {
}

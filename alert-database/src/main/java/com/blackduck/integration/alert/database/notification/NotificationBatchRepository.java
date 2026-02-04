package com.blackduck.integration.alert.database.notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationBatchRepository extends JpaRepository<NotificationBatchEntity, NotificationBatchPK> {
}

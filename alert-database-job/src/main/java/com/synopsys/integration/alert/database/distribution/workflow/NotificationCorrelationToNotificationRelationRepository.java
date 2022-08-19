package com.synopsys.integration.alert.database.distribution.workflow;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationCorrelationToNotificationRelationRepository
    extends JpaRepository<NotificationCorrelationToNotificationRelation, NotificationCorrelationToNotificationRelationPK> {

}

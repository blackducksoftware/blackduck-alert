package com.synopsys.integration.alert.database.distribution.workflow;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;

@Entity
@IdClass(NotificationCorrelationToNotificationRelationPK.class)
@Table(schema = "alert", name = "notification_correlation_to_notification_relation")
public class NotificationCorrelationToNotificationRelation extends DatabaseRelation {

    @Id
    @Column(name = "notification_correlation_id")
    private UUID notificationCorrelationId;

    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    public NotificationCorrelationToNotificationRelation() {
    }

    public NotificationCorrelationToNotificationRelation(UUID notificationCorrelationId, Long notificationId) {
        this.notificationCorrelationId = notificationCorrelationId;
        this.notificationId = notificationId;
    }

    public UUID getNotificationCorrelationId() {
        return notificationCorrelationId;
    }

    public Long getNotificationId() {
        return notificationId;
    }
}

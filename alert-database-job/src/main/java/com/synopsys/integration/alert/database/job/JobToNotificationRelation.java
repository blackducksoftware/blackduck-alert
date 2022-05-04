/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.DatabaseRelation;

@Entity
@IdClass(JobToNotificationRelationPK.class)
@Table(schema = "alert", name = "job_notification_relation")
public class JobToNotificationRelation extends DatabaseRelation {
    @Id
    @Column(name = "correlation_id")
    private UUID correlationId;

    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Id
    @Column(name = "notification_id")
    private Long notificationId;

    public JobToNotificationRelation() {
    }

    public JobToNotificationRelation(UUID correlationId, UUID jobId, Long notificationId) {
        this.correlationId = correlationId;
        this.jobId = jobId;
        this.notificationId = notificationId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getNotificationId() {
        return notificationId;
    }
}

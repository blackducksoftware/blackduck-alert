/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.job.blackduck.notification;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(BlackDuckJobNotificationTypePK.class)
@Table(schema = "alert", name = "blackduck_job_notification_types")
public class BlackDuckJobNotificationTypeEntity {
    @Id
    @Column(name = "job_id")
    private UUID jobId;

    @Id
    @Column(name = "notification_type")
    private String notificationType;

    public BlackDuckJobNotificationTypeEntity() {
    }

    public BlackDuckJobNotificationTypeEntity(UUID jobId, String notificationType) {
        this.jobId = jobId;
        this.notificationType = notificationType;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

}

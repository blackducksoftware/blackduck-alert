package com.synopsys.integration.alert.database.distribution.workflow;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "job_sub_task_status")
public class JobSubTaskStatusEntity extends BaseEntity {
    private static final long serialVersionUID = -6039296175355842298L;
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "job_id")
    private UUID jobId;
    @Column(name = "remaining_event_count")
    private Long remainingEvents;

    @Column(name = "notification_correlation_id")
    private UUID notificationCorrelationId;

    public JobSubTaskStatusEntity() {
    }

    public JobSubTaskStatusEntity(UUID id, UUID jobId, Long remainingEvents, UUID notificationCorrelationId) {
        this.id = id;
        this.jobId = jobId;
        this.remainingEvents = remainingEvents;
        this.notificationCorrelationId = notificationCorrelationId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getJobId() {
        return jobId;
    }

    public Long getRemainingEvents() {
        return remainingEvents;
    }

    public UUID getNotificationCorrelationId() {
        return notificationCorrelationId;
    }
}

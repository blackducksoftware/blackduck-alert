package com.synopsys.integration.alert.database.job.execution;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "job_execution_status")
public class JobExecutionStatusEntity extends BaseEntity {
    private static final long serialVersionUID = -3107164032971829096L;
    @Id
    @Column(name = "job_config_id")
    private UUID jobConfigId;

    @Column(name = "notification_count")
    private Long notificationCount;
    @Column(name = "success_count")
    private Long successCount;
    @Column(name = "failure_count")
    private Long failureCount;

    @Column(name = "latest_status")
    private String latestStatus;
    @Column(name = "last_run")
    private OffsetDateTime lastRun;

    public JobExecutionStatusEntity() {
        // default constructor for JPA
    }

    public JobExecutionStatusEntity(
        UUID jobConfigId,
        Long notificationCount,
        Long successCount,
        Long failureCount,
        String latestStatus,
        OffsetDateTime lastRun
    ) {
        this.jobConfigId = jobConfigId;
        this.notificationCount = notificationCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.latestStatus = latestStatus;
        this.lastRun = lastRun;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public Long getNotificationCount() {
        return notificationCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public String getLatestStatus() {
        return latestStatus;
    }

    public OffsetDateTime getLastRun() {
        return lastRun;
    }

}

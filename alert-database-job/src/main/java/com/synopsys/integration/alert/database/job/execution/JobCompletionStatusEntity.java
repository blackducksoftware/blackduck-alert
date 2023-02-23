package com.synopsys.integration.alert.database.job.execution;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "job_completion_status")
public class JobCompletionStatusEntity extends BaseEntity {
    private static final long serialVersionUID = -3107164032971829096L;
    @Id
    @Column(name = "job_config_id")
    private UUID jobConfigId;

    @Column(name = "latest_notification_count")
    private long latestNotificationCount;

    @Column(name = "total_notification_count")
    private long totalNotificationCount;
    @Column(name = "success_count")
    private long successCount;
    @Column(name = "failure_count")
    private long failureCount;

    @Column(name = "latest_status")
    private String latestStatus;
    @Column(name = "last_run")
    private OffsetDateTime lastRun;

    public JobCompletionStatusEntity() {
        // default constructor for JPA
    }

    public JobCompletionStatusEntity(
        UUID jobConfigId,
        long latestNotificationCount,
        long totalNotificationCount,
        long successCount,
        long failureCount,
        String latestStatus,
        OffsetDateTime lastRun
    ) {
        this.jobConfigId = jobConfigId;
        this.latestNotificationCount = latestNotificationCount;
        this.totalNotificationCount = totalNotificationCount;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.latestStatus = latestStatus;
        this.lastRun = lastRun;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public long getLatestNotificationCount() {
        return latestNotificationCount;
    }

    public long getTotalNotificationCount() {
        return totalNotificationCount;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public String getLatestStatus() {
        return latestStatus;
    }

    public OffsetDateTime getLastRun() {
        return lastRun;
    }

}

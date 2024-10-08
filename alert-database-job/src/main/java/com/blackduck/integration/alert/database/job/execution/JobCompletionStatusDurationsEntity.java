package com.blackduck.integration.alert.database.job.execution;

import java.util.UUID;

import com.blackduck.integration.alert.database.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "job_completion_durations")
public class JobCompletionStatusDurationsEntity extends BaseEntity {
    private static final long serialVersionUID = 8165889267435905900L;
    @Id
    @Column(name = "job_config_id")
    private UUID jobConfigId;

    @Column(name = "job_duration_nanos")
    private long jobDuration;

    @Column(name = "notification_processing_duration_nanos")
    private long notificationProcessingDuration;

    @Column(name = "channel_processing_duration_nanos")
    private long channelProcessingDuration;

    @Column(name = "issue_creation_duration_nanos")
    private long issueCreationDuration;

    @Column(name = "issue_commenting_duration_nanos")
    private long issueCommentingDuration;

    @Column(name = "issue_resolving_duration_nanos")
    private long issueTransitionDuration;

    public JobCompletionStatusDurationsEntity() {
        //default constructor for JPA
    }

    public JobCompletionStatusDurationsEntity(
        UUID jobConfigId,
        long jobDuration,
        long notificationProcessingDuration,
        long channelProcessingDuration,
        long issueCreationDuration,
        long issueCommentingDuration,
        long issueTransitionDuration
    ) {
        this.jobConfigId = jobConfigId;
        this.jobDuration = jobDuration;
        this.notificationProcessingDuration = notificationProcessingDuration;
        this.channelProcessingDuration = channelProcessingDuration;
        this.issueCreationDuration = issueCreationDuration;
        this.issueCommentingDuration = issueCommentingDuration;
        this.issueTransitionDuration = issueTransitionDuration;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public long getJobDuration() {
        return jobDuration;
    }

    public long getNotificationProcessingDuration() {
        return notificationProcessingDuration;
    }

    public long getChannelProcessingDuration() {
        return channelProcessingDuration;
    }

    public long getIssueCreationDuration() {
        return issueCreationDuration;
    }

    public long getIssueCommentingDuration() {
        return issueCommentingDuration;
    }

    public long getIssueTransitionDuration() {
        return issueTransitionDuration;
    }
}

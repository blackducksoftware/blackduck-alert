package com.synopsys.integration.alert.database.job.execution;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.synopsys.integration.alert.database.BaseEntity;

@Entity
@Table(schema = "alert", name = "job_execution_durations")
public class JobExecutionStatusDurationsEntity extends BaseEntity {
    private static final long serialVersionUID = 8165889267435905900L;
    @Id
    @Column(name = "job_config_id")
    private UUID jobConfigId;

    @Column(name = "job_duration_milliseconds")
    private Long jobDurationMillisec;

    @Column(name = "notification_processing_duration_milliseconds")
    private Long notificationProcessingDuration;

    @Column(name = "channel_processing_duration_milliseconds")
    private Long channelProcessingDuration;

    @Column(name = "issue_creation_duration_milliseconds")
    private Long issueCreationDuration;

    @Column(name = "issue_commenting_duration_milliseconds")
    private Long issueCommentingDuration;

    @Column(name = "issue_resolving_duration_milliseconds")
    private Long issueTransitionDuration;

    public JobExecutionStatusDurationsEntity() {
        //default constructor for JPA
    }

    public JobExecutionStatusDurationsEntity(
        UUID jobConfigId,
        Long jobDurationMillisec,
        Long notificationProcessingDuration,
        Long channelProcessingDuration,
        Long issueCreationDuration,
        Long issueCommentingDuration,
        Long issueTransitionDuration
    ) {
        this.jobConfigId = jobConfigId;
        this.jobDurationMillisec = jobDurationMillisec;
        this.notificationProcessingDuration = notificationProcessingDuration;
        this.channelProcessingDuration = channelProcessingDuration;
        this.issueCreationDuration = issueCreationDuration;
        this.issueCommentingDuration = issueCommentingDuration;
        this.issueTransitionDuration = issueTransitionDuration;
    }

    public UUID getJobConfigId() {
        return jobConfigId;
    }

    public Long getJobDurationMillisec() {
        return jobDurationMillisec;
    }

    public Long getNotificationProcessingDuration() {
        return notificationProcessingDuration;
    }

    public Long getChannelProcessingDuration() {
        return channelProcessingDuration;
    }

    public Long getIssueCreationDuration() {
        return issueCreationDuration;
    }

    public Long getIssueCommentingDuration() {
        return issueCommentingDuration;
    }

    public Long getIssueTransitionDuration() {
        return issueTransitionDuration;
    }
}

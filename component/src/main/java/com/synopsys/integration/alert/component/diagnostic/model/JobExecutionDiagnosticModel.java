package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

public class JobExecutionDiagnosticModel {
    private final String jobName;
    private final String channelName;
    private final String start;
    private final String end;
    private final AuditEntryStatus status;
    private final Long notificationCount;

    private final List<JobStageDiagnosticModel> stages;

    public JobExecutionDiagnosticModel(
        final String jobName,
        final String channelName,
        final String start,
        final String end,
        final AuditEntryStatus status,
        final Long notificationCount,
        final List<JobStageDiagnosticModel> stages
    ) {
        this.jobName = jobName;
        this.channelName = channelName;
        this.start = start;
        this.end = end;
        this.status = status;
        this.notificationCount = notificationCount;
        this.stages = stages;
    }

    public String getJobName() {
        return jobName;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public AuditEntryStatus getStatus() {
        return status;
    }

    public Long getNotificationCount() {
        return notificationCount;
    }

    public List<JobStageDiagnosticModel> getStages() {
        return stages;
    }
}

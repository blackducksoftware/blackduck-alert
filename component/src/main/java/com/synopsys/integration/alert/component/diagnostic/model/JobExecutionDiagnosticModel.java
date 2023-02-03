package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.AuditEntryStatus;

public class JobExecutionDiagnosticModel {
    private final String jobName;
    private final String channelName;
    private final String start;
    private final String end;
    private final AuditEntryStatus status;
    private final int processedNotificationCount;
    private final int totalNotificationCount;

    private final int remainingEvents;

    private final List<JobStageDiagnosticModel> stages;

    public JobExecutionDiagnosticModel(
        String jobName,
        String channelName,
        String start,
        String end,
        AuditEntryStatus status,
        int processedNotificationCount,
        int totalNotificationCount,
        int remainingEvents,
        List<JobStageDiagnosticModel> stages
    ) {
        this.jobName = jobName;
        this.channelName = channelName;
        this.start = start;
        this.end = end;
        this.status = status;
        this.processedNotificationCount = processedNotificationCount;
        this.totalNotificationCount = totalNotificationCount;
        this.remainingEvents = remainingEvents;
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

    public int getProcessedNotificationCount() {
        return processedNotificationCount;
    }

    public int getTotalNotificationCount() {
        return totalNotificationCount;
    }

    public int getRemainingEvents() {
        return remainingEvents;
    }

    public List<JobStageDiagnosticModel> getStages() {
        return stages;
    }
}

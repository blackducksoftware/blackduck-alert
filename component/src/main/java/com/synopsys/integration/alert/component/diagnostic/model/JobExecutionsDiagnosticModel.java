package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.List;

public class JobExecutionsDiagnosticModel {
    private final long totalJobsInSystem;
    private final long pendingJobs;
    private final long successFulJobs;
    private final long failedJobs;

    private final List<JobExecutionDiagnosticModel> jobExecutionDiagnosticModels;

    public JobExecutionsDiagnosticModel(
        final long totalJobsInSystem,
        final long pendingJobs,
        final long successFulJobs,
        final long failedJobs,
        final List<JobExecutionDiagnosticModel> jobExecutionDiagnosticModels
    ) {
        this.totalJobsInSystem = totalJobsInSystem;
        this.pendingJobs = pendingJobs;
        this.successFulJobs = successFulJobs;
        this.failedJobs = failedJobs;
        this.jobExecutionDiagnosticModels = jobExecutionDiagnosticModels;
    }

    public long getTotalJobsInSystem() {
        return totalJobsInSystem;
    }

    public long getPendingJobs() {
        return pendingJobs;
    }

    public long getSuccessFulJobs() {
        return successFulJobs;
    }

    public long getFailedJobs() {
        return failedJobs;
    }

    public List<JobExecutionDiagnosticModel> getJobExecutionDiagnosticModels() {
        return jobExecutionDiagnosticModels;
    }
}

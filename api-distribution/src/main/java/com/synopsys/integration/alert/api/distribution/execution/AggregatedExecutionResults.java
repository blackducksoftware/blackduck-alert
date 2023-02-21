package com.synopsys.integration.alert.api.distribution.execution;

public class AggregatedExecutionResults {
    private final long totalJobsInSystem;
    private final long pendingJobs;
    private final long successFulJobs;
    private final long failedJobs;

    public AggregatedExecutionResults(long totalJobsInSystem, long pendingJobs, long successFulJobs, long failedJobs) {
        this.totalJobsInSystem = totalJobsInSystem;
        this.pendingJobs = pendingJobs;
        this.successFulJobs = successFulJobs;
        this.failedJobs = failedJobs;
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
}

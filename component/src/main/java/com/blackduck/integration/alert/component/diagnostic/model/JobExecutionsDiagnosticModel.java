/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class JobExecutionsDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = -4435155091820514569L;

    private final long totalJobsInSystem;
    private final long pendingJobs;
    private final long successFulJobs;
    private final long failedJobs;

    private final List<JobExecutionDiagnosticModel> jobExecutions;

    public JobExecutionsDiagnosticModel(
            long totalJobsInSystem,
            long pendingJobs,
            long successFulJobs,
            long failedJobs,
            List<JobExecutionDiagnosticModel> jobExecutions
    ) {
        this.totalJobsInSystem = totalJobsInSystem;
        this.pendingJobs = pendingJobs;
        this.successFulJobs = successFulJobs;
        this.failedJobs = failedJobs;
        this.jobExecutions = jobExecutions;
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

    public List<JobExecutionDiagnosticModel> getJobExecutions() {
        return jobExecutions;
    }
}

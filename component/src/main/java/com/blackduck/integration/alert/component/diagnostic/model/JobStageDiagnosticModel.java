/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.model;

import com.blackduck.integration.alert.api.distribution.execution.JobStage;

public class JobStageDiagnosticModel {
    public final JobStage stage;
    public final String start;
    public final String end;

    public JobStageDiagnosticModel(JobStage stage, String start, String end) {
        this.stage = stage;
        this.start = start;
        this.end = end;
    }

    public JobStage getStage() {
        return stage;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }
}

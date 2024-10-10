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

public class CompletedJobDurationDiagnosticModel extends AlertSerializableModel {

    private static final long serialVersionUID = -7963260099077205918L;

    private final String jobDuration;

    private final List<CompletedJobStageDurationModel> stageDurations;

    public CompletedJobDurationDiagnosticModel(
        String jobDuration,
        List<CompletedJobStageDurationModel> stageDurations
    ) {
        this.jobDuration = jobDuration;
        this.stageDurations = stageDurations;
    }

    public String getJobDuration() {
        return jobDuration;
    }

    public List<CompletedJobStageDurationModel> getStageDurations() {
        return stageDurations;
    }
}

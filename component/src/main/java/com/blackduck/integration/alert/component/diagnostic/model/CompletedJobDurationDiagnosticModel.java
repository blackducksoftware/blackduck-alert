package com.blackduck.integration.alert.component.diagnostic.model;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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

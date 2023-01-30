package com.synopsys.integration.alert.component.diagnostic.model;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class CompletedJobsDiagnosticModel extends AlertSerializableModel {
    private final List<CompletedJobDiagnosticModel> completedJobs;

    public CompletedJobsDiagnosticModel(List<CompletedJobDiagnosticModel> completedJobs) {
        this.completedJobs = completedJobs;
    }

    public List<CompletedJobDiagnosticModel> getCompletedJobs() {
        return completedJobs;
    }
}

package com.blackduck.integration.alert.component.diagnostic.model;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class CompletedJobsDiagnosticModel extends AlertSerializableModel {
    private static final long serialVersionUID = 8057080793182640630L;

    private final List<CompletedJobDiagnosticModel> completedJobs;

    public CompletedJobsDiagnosticModel(List<CompletedJobDiagnosticModel> completedJobs) {
        this.completedJobs = completedJobs;
    }

    public List<CompletedJobDiagnosticModel> getCompletedJobs() {
        return completedJobs;
    }
}

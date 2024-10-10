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

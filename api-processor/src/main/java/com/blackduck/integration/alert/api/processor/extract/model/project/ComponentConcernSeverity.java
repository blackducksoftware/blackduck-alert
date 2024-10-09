/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.extract.model.project;

public enum ComponentConcernSeverity {
    BLOCKER("BLOCKER", Constants.CRITICAL_SEVERITY_TEXT),
    CRITICAL(Constants.CRITICAL_SEVERITY_TEXT, Constants.CRITICAL_SEVERITY_TEXT),
    MAJOR_HIGH("MAJOR", "HIGH"),
    MINOR_MEDIUM("MINOR", "MEDIUM"),
    TRIVIAL_LOW("TRIVIAL", "LOW"),
    UNSPECIFIED_UNKNOWN("UNSPECIFIED", "UNKNOWN");

    private final String policyLabel;
    private final String vulnerabilityLabel;

    ComponentConcernSeverity(String policyLabel, String vulnerabilityLabel) {
        this.vulnerabilityLabel = vulnerabilityLabel;
        this.policyLabel = policyLabel;
    }

    public String getPolicyLabel() {
        return policyLabel;
    }

    public String getVulnerabilityLabel() {
        return vulnerabilityLabel;
    }

    private static class Constants {
        private static final String CRITICAL_SEVERITY_TEXT = "CRITICAL";

    }

}

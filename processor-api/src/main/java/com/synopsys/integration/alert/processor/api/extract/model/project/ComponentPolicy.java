/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

public class ComponentPolicy {
    private final String policyName;
    private final ComponentConcernSeverity severity;
    private final boolean overridden;

    public ComponentPolicy(String policyName, ComponentConcernSeverity severity, boolean overridden) {
        this.policyName = policyName;
        this.severity = severity;
        this.overridden = overridden;
    }

    public String getPolicyName() {
        return policyName;
    }

    public ComponentConcernSeverity getSeverity() {
        return severity;
    }

    public boolean isOverridden() {
        return overridden;
    }

}

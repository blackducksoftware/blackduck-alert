/*
 * api-processor
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class ComponentPolicy extends AlertSerializableModel {
    private final String policyName;
    private final ComponentConcernSeverity severity;
    private final boolean overridden;
    private final boolean vulnerabilityPolicy;
    private final String description;

    public ComponentPolicy(String policyName, ComponentConcernSeverity severity, boolean overridden, boolean vulnerabilityPolicy, @Nullable String description) {
        this.policyName = policyName;
        this.severity = severity;
        this.overridden = overridden;
        this.vulnerabilityPolicy = vulnerabilityPolicy;
        this.description = description;
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

    public boolean isVulnerabilityPolicy() {
        return vulnerabilityPolicy;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }
}

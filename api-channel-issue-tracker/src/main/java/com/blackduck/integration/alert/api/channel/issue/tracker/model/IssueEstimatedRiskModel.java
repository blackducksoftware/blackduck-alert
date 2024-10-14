/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernType;

public class IssueEstimatedRiskModel extends AlertSerializableModel {
    private static final long serialVersionUID = -8833421886887540364L;
    private final ComponentConcernSeverity severity;
    private final Number count;
    private final String name;
    private final String componentVersionUrl;

    public static IssueEstimatedRiskModel fromComponentConcern(ComponentConcern componentConcern) {
        ComponentConcernType componentConcernType = componentConcern.getType();
        if (!ComponentConcernType.UNKNOWN_VERSION.equals(componentConcernType)) {
            throw new IllegalArgumentException(String.format("Expected a %s ComponentConcern, but this was a %s", ComponentConcernType.UNKNOWN_VERSION.name(), componentConcernType));
        }

        return new IssueEstimatedRiskModel(
            componentConcern.getSeverity(),
            componentConcern.getNumericValue(),
            componentConcern.getName(),
            componentConcern.getUrl().orElse(null)
        );
    }

    public IssueEstimatedRiskModel(ComponentConcernSeverity severity, Number count, String name, @Nullable String componentversionUrl) {
        this.severity = severity;
        this.count = count;
        this.name = name;
        this.componentVersionUrl = componentversionUrl;
    }

    public ComponentConcernSeverity getSeverity() {
        return severity;
    }

    public Number getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getComponentVersionUrl() {
        return Optional.ofNullable(componentVersionUrl);
    }
}

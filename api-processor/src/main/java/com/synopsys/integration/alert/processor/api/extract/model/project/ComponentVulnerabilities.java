/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class ComponentVulnerabilities extends AlertSerializableModel {
    private final List<LinkableItem> critical;
    private final List<LinkableItem> high;
    private final List<LinkableItem> medium;
    private final List<LinkableItem> low;

    public static ComponentVulnerabilities none() {
        return new ComponentVulnerabilities(List.of(), List.of(), List.of(), List.of());
    }

    public ComponentVulnerabilities(List<LinkableItem> critical, List<LinkableItem> high, List<LinkableItem> medium, List<LinkableItem> low) {
        this.critical = critical;
        this.high = high;
        this.medium = medium;
        this.low = low;
    }

    public List<LinkableItem> getCritical() {
        return critical;
    }

    public List<LinkableItem> getHigh() {
        return high;
    }

    public List<LinkableItem> getMedium() {
        return medium;
    }

    public List<LinkableItem> getLow() {
        return low;
    }

    public boolean hasVulnerabilities() {
        return !critical.isEmpty() || !high.isEmpty() || !medium.isEmpty() || !low.isEmpty();
    }

    public Optional<ComponentConcernSeverity> computeHighestSeverity() {
        ComponentConcernSeverity severity = null;
        if (hasVulnerabilities()) {
            if (!getCritical().isEmpty()) {
                severity = ComponentConcernSeverity.CRITICAL;
            } else if (!getHigh().isEmpty()) {
                severity = ComponentConcernSeverity.MAJOR_HIGH;
            } else if (!getMedium().isEmpty()) {
                severity = ComponentConcernSeverity.MINOR_MEDIUM;
            } else if (!getLow().isEmpty()) {
                severity = ComponentConcernSeverity.TRIVIAL_LOW;
            }
        }
        return Optional.ofNullable(severity);
    }
}

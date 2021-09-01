/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.List;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

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

}
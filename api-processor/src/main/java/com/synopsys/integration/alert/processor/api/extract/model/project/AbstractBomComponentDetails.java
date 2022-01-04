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

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public abstract class AbstractBomComponentDetails extends AlertSerializableModel {
    private final LinkableItem component;
    private final LinkableItem componentVersion;

    private final ComponentVulnerabilities componentVulnerabilities;
    private final List<ComponentPolicy> relevantPolicies;

    private final LinkableItem license;
    private final String usage;
    private final ComponentUpgradeGuidance componentUpgradeGuidance;
    private final List<LinkableItem> additionalAttributes;

    private final String blackDuckIssuesUrl;

    protected AbstractBomComponentDetails(
        LinkableItem component,
        @Nullable LinkableItem componentVersion,
        ComponentVulnerabilities componentVulnerabilities,
        List<ComponentPolicy> relevantPolicies,
        LinkableItem license,
        String usage,
        ComponentUpgradeGuidance componentUpgradeGuidance,
        List<LinkableItem> additionalAttributes,
        String blackDuckIssuesUrl
    ) {
        this.component = component;
        this.componentVersion = componentVersion;
        this.componentVulnerabilities = componentVulnerabilities;
        this.relevantPolicies = relevantPolicies;
        this.license = license;
        this.usage = usage;
        this.componentUpgradeGuidance = componentUpgradeGuidance;
        this.additionalAttributes = additionalAttributes;
        this.blackDuckIssuesUrl = blackDuckIssuesUrl;
    }

    public LinkableItem getComponent() {
        return component;
    }

    public Optional<LinkableItem> getComponentVersion() {
        return Optional.ofNullable(componentVersion);
    }

    public ComponentVulnerabilities getComponentVulnerabilities() {
        return componentVulnerabilities;
    }

    public List<ComponentPolicy> getRelevantPolicies() {
        return relevantPolicies;
    }

    public LinkableItem getLicense() {
        return license;
    }

    public String getUsage() {
        return usage;
    }

    public ComponentUpgradeGuidance getComponentUpgradeGuidance() {
        return componentUpgradeGuidance;
    }

    public List<LinkableItem> getAdditionalAttributes() {
        return additionalAttributes;
    }

    // TODO make optional
    public String getBlackDuckIssuesUrl() {
        return blackDuckIssuesUrl;
    }

}

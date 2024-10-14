/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.channel.issue.tracker.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.api.processor.extract.model.project.AbstractBomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentVulnerabilities;

public class IssueBomComponentDetails extends AbstractBomComponentDetails {
    private static final String UNKNOWN_USAGE = "Unknown Usage";
    private static final LinkableItem UNKNOWN_LICENSE = new LinkableItem("License", "Unknown License");

    public static IssueBomComponentDetails fromSearchResults(LinkableItem component, @Nullable LinkableItem componentVersion) {
        return new IssueBomComponentDetails(
            component,
            componentVersion,
            ComponentVulnerabilities.none(),
            List.of(),
            UNKNOWN_LICENSE,
            UNKNOWN_USAGE,
            ComponentUpgradeGuidance.none(),
            List.of(),
            ""
        );
    }

    public static IssueBomComponentDetails fromBomComponentDetails(AbstractBomComponentDetails bomComponentDetails) {
        return new IssueBomComponentDetails(
            bomComponentDetails.getComponent(),
            bomComponentDetails.getComponentVersion().orElse(null),
            bomComponentDetails.getComponentVulnerabilities(),
            bomComponentDetails.getRelevantPolicies(),
            bomComponentDetails.getLicense(),
            bomComponentDetails.getUsage(),
            bomComponentDetails.getComponentUpgradeGuidance(),
            bomComponentDetails.getAdditionalAttributes(),
            bomComponentDetails.getBlackDuckIssuesUrl()
        );
    }

    private IssueBomComponentDetails(
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
        super(component, componentVersion, componentVulnerabilities, relevantPolicies, license, usage, componentUpgradeGuidance, additionalAttributes, blackDuckIssuesUrl);
    }

}

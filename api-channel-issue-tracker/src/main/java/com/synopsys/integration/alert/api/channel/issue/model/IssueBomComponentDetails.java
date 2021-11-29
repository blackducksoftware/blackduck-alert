/*
 * api-channel-issue-tracker
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.channel.issue.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;

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
            List.of(),
            ""
        );
    }

    public static IssueBomComponentDetails fromBomComponentDetails(AbstractBomComponentDetails bomComponentDetails) {
        return new IssueBomComponentDetails(
            bomComponentDetails.getComponent(),
            bomComponentDetails.getComponentVersion().orElse(null),
            bomComponentDetails.getComponentVulnerabilities(),
            bomComponentDetails.getComponentPolicies(),
            bomComponentDetails.getLicense(),
            bomComponentDetails.getUsage(),
            bomComponentDetails.getAdditionalAttributes(),
            bomComponentDetails.getBlackDuckIssuesUrl()
        );
    }

    private IssueBomComponentDetails(
        LinkableItem component,
        @Nullable LinkableItem componentVersion,
        ComponentVulnerabilities componentVulnerabilities,
        List<ComponentPolicy> componentPolicies,
        LinkableItem license,
        String usage,
        List<LinkableItem> additionalAttributes,
        String blackDuckIssuesUrl
    ) {
        super(component, componentVersion, componentVulnerabilities, componentPolicies, license, usage, additionalAttributes, blackDuckIssuesUrl);
    }

}

/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.api.issue.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.AbstractBomComponentDetails;

public class IssueBomComponentDetails extends AbstractBomComponentDetails {
    private static final String UNKNOWN_USAGE = "Unknown Usage";
    private static final LinkableItem UNKNOWN_LICENSE = new LinkableItem("License", "Unknown License");

    public static IssueBomComponentDetails fromSearchResults(LinkableItem component, @Nullable LinkableItem componentVersion) {
        return new IssueBomComponentDetails(
            component,
            componentVersion,
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
            bomComponentDetails.getLicense(),
            bomComponentDetails.getUsage(),
            bomComponentDetails.getAdditionalAttributes(),
            bomComponentDetails.getBlackDuckIssuesUrl()
        );
    }

    public IssueBomComponentDetails(LinkableItem component, @Nullable LinkableItem componentVersion, LinkableItem license, String usage, List<LinkableItem> additionalAttributes, String blackDuckIssuesUrl) {
        super(component, componentVersion, license, usage, additionalAttributes, blackDuckIssuesUrl);
    }

}

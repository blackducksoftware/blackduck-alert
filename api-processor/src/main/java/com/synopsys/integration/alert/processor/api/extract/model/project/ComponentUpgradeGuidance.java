/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class ComponentUpgradeGuidance extends AlertSerializableModel {
    private final UpgradeGuidanceDetails shortTermUpgradeGuidanceDetails;
    private final UpgradeGuidanceDetails longTermUpgradeGuidanceDetails;
    private final String originExternalId;
    private final String componentName;
    private final String componentVersion;

    public static ComponentUpgradeGuidance none() {
        return new ComponentUpgradeGuidance(UpgradeGuidanceDetails.none(), UpgradeGuidanceDetails.none(), null, null, null);
    }

    public ComponentUpgradeGuidance(
        UpgradeGuidanceDetails shortTermUpgradeGuidanceDetails,
        UpgradeGuidanceDetails longTermUpgradeGuidanceDetails,
        String originExternalId,
        String componentName,
        String componentVersion
    ) {
        this.shortTermUpgradeGuidanceDetails = shortTermUpgradeGuidanceDetails;
        this.longTermUpgradeGuidanceDetails = longTermUpgradeGuidanceDetails;
        this.originExternalId = originExternalId;
        this.componentName = componentName;
        this.componentVersion = componentVersion;
    }

    //TODO: These methods remain consistent with the old api to avoid breaking changes. This should be resolved in the future.
    public Optional<LinkableItem> getShortTermUpgradeGuidance() {
        return shortTermUpgradeGuidanceDetails.getUpgradeGuidanceLink();
    }

    public Optional<LinkableItem> getLongTermUpgradeGuidance() {
        return longTermUpgradeGuidanceDetails.getUpgradeGuidanceLink();
    }

    public UpgradeGuidanceDetails getShortTermUpgradeGuidanceDetails() {
        return shortTermUpgradeGuidanceDetails;
    }

    public UpgradeGuidanceDetails getLongTermUpgradeGuidanceDetails() {
        return longTermUpgradeGuidanceDetails;
    }

    public String getOriginExternalId() {
        return originExternalId;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersion;
    }
}


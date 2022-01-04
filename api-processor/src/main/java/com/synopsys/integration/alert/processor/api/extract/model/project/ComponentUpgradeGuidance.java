/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class ComponentUpgradeGuidance extends AlertSerializableModel {
    private final LinkableItem shortTermUpgradeGuidance;
    private final LinkableItem longTermUpgradeGuidance;

    public static ComponentUpgradeGuidance none() {
        return new ComponentUpgradeGuidance(null, null);
    }

    public ComponentUpgradeGuidance(@Nullable LinkableItem shortTermUpgradeGuidance, @Nullable LinkableItem longTermUpgradeGuidance) {
        this.shortTermUpgradeGuidance = shortTermUpgradeGuidance;
        this.longTermUpgradeGuidance = longTermUpgradeGuidance;
    }

    public Optional<LinkableItem> getShortTermUpgradeGuidance() {
        return Optional.ofNullable(shortTermUpgradeGuidance);
    }

    public Optional<LinkableItem> getLongTermUpgradeGuidance() {
        return Optional.ofNullable(longTermUpgradeGuidance);
    }
}


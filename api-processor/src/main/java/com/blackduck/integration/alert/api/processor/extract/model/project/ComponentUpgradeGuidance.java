/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.processor.extract.model.project;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

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


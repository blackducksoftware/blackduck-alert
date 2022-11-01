package com.synopsys.integration.alert.processor.api.extract.model.project;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class UpgradeGuidanceDetails extends AlertSerializableModel {
    private final LinkableItem upgradeGuidanceLink;
    private final String originExternalId;
    private final String componentVersion;
    private final String vulnerabilityRisk;

    public static UpgradeGuidanceDetails none() {
        return new UpgradeGuidanceDetails(null, null, null, null);
    }

    public UpgradeGuidanceDetails(
        @Nullable LinkableItem upgradeGuidanceLink,
        @Nullable String originExternalId,
        @Nullable String componentVersion,
        @Nullable String vulnerabilityRisk
    ) {
        this.upgradeGuidanceLink = upgradeGuidanceLink;
        this.originExternalId = originExternalId;
        this.componentVersion = componentVersion;
        this.vulnerabilityRisk = vulnerabilityRisk;
    }

    public Optional<LinkableItem> getUpgradeGuidanceLink() {
        return Optional.ofNullable(upgradeGuidanceLink);
    }

    public Optional<String> getOriginExternalId() {
        return Optional.ofNullable(originExternalId);
    }

    public Optional<String> getComponentVersion() {
        return Optional.ofNullable(componentVersion);
    }

    public Optional<String> getVulnerabilityRisk() {
        return Optional.ofNullable(vulnerabilityRisk);
    }
}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message.service;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermView;
import com.blackduck.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermView;

public class CommonUpgradeGuidanceModel extends AlertSerializableModel {
    private final String origin;
    private final String originExternalId;
    private final String originExternalNamespace;
    private final String originName;
    private final String version;
    private final String versionName;
    private final UpgradeGuidanceRiskModel vulnerabilityRisk;

    public static CommonUpgradeGuidanceModel fromShortTermGuidance(ComponentVersionUpgradeGuidanceShortTermView upgradeGuidanceShortTermView) {
        return new CommonUpgradeGuidanceModel(
            upgradeGuidanceShortTermView.getOrigin(),
            upgradeGuidanceShortTermView.getOriginExternalId(),
            upgradeGuidanceShortTermView.getOriginExternalNamespace(),
            upgradeGuidanceShortTermView.getOriginName(),
            upgradeGuidanceShortTermView.getVersion(),
            upgradeGuidanceShortTermView.getVersionName(),
            UpgradeGuidanceRiskModel.fromShortTermVulnerabilityRiskView(upgradeGuidanceShortTermView.getVulnerabilityRisk())
        );
    }

    public static CommonUpgradeGuidanceModel fromLongTermGuidance(ComponentVersionUpgradeGuidanceLongTermView upgradeGuidanceLongTermView) {
        return new CommonUpgradeGuidanceModel(
            upgradeGuidanceLongTermView.getOrigin(),
            upgradeGuidanceLongTermView.getOriginExternalId(),
            upgradeGuidanceLongTermView.getOriginExternalNamespace(),
            upgradeGuidanceLongTermView.getOriginName(),
            upgradeGuidanceLongTermView.getVersion(),
            upgradeGuidanceLongTermView.getVersionName(),
            UpgradeGuidanceRiskModel.fromLongTermVulnerabilityRiskView(upgradeGuidanceLongTermView.getVulnerabilityRisk())
        );
    }

    public CommonUpgradeGuidanceModel(
        String origin,
        String originExternalId,
        String originExternalNamespace,
        String originName,
        String version,
        String versionName,
        UpgradeGuidanceRiskModel vulnerabilityRisk
    ) {
        this.origin = origin;
        this.originExternalId = originExternalId;
        this.originExternalNamespace = originExternalNamespace;
        this.originName = originName;
        this.version = version;
        this.versionName = versionName;
        this.vulnerabilityRisk = vulnerabilityRisk;
    }

    public String getOrigin() {
        return origin;
    }

    public String getOriginExternalId() {
        return originExternalId;
    }

    public String getOriginExternalNamespace() {
        return originExternalNamespace;
    }

    public String getOriginName() {
        return originName;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionName() {
        return versionName;
    }

    public UpgradeGuidanceRiskModel getVulnerabilityRisk() {
        return vulnerabilityRisk;
    }

}

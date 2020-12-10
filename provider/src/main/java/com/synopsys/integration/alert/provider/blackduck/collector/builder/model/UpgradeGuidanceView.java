package com.synopsys.integration.alert.provider.blackduck.collector.builder.model;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermView;

public class UpgradeGuidanceView extends AlertSerializableModel {
    private String origin;
    private String originExternalId;
    private String originExternalNamespace;
    private String originName;
    private String version;
    private String versionName;
    private UpgradeGuidanceRiskView vulnerabilityRisk;

    public static UpgradeGuidanceView fromShortTermGuidance(ComponentVersionUpgradeGuidanceShortTermView upgradeGuidanceShortTermView) {
        if (null == upgradeGuidanceShortTermView) {
            return null;
        }
        return new UpgradeGuidanceView(upgradeGuidanceShortTermView.getOrigin(), upgradeGuidanceShortTermView.getOriginExternalId(), upgradeGuidanceShortTermView.getOriginExternalNamespace(),
            upgradeGuidanceShortTermView.getOriginName(), upgradeGuidanceShortTermView.getVersion(), upgradeGuidanceShortTermView.getVersionName(),
            UpgradeGuidanceRiskView.fromShortTermVulnerabilityRiskView(upgradeGuidanceShortTermView.getVulnerabilityRisk()));
    }

    public static UpgradeGuidanceView fromLongTermGuidance(ComponentVersionUpgradeGuidanceLongTermView upgradeGuidanceLongTermView) {
        if (null == upgradeGuidanceLongTermView) {
            return null;
        }
        return new UpgradeGuidanceView(upgradeGuidanceLongTermView.getOrigin(), upgradeGuidanceLongTermView.getOriginExternalId(), upgradeGuidanceLongTermView.getOriginExternalNamespace(),
            upgradeGuidanceLongTermView.getOriginName(), upgradeGuidanceLongTermView.getVersion(), upgradeGuidanceLongTermView.getVersionName(),
            UpgradeGuidanceRiskView.fromLongTermVulnerabilityRiskView(upgradeGuidanceLongTermView.getVulnerabilityRisk()));
    }

    private UpgradeGuidanceView(String origin, String originExternalId, String originExternalNamespace, String originName, String version, String versionName,
        UpgradeGuidanceRiskView vulnerabilityRisk) {
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

    public UpgradeGuidanceRiskView getVulnerabilityRisk() {
        return vulnerabilityRisk;
    }

}

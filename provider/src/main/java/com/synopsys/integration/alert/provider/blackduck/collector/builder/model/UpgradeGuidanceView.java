/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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

    public UpgradeGuidanceView(String origin, String originExternalId, String originExternalNamespace, String originName, String version, String versionName,
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

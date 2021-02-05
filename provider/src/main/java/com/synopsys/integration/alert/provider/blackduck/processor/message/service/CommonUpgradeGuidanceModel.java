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
package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermView;

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

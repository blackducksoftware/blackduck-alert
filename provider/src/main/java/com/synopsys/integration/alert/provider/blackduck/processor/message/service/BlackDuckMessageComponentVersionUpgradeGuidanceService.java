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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.synopsys.integration.blackduck.api.generated.component.RemediatingVersionView;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionRemediatingView;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionUpgradeGuidanceView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.service.dataservice.ComponentService;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckMessageComponentVersionUpgradeGuidanceService {
    private final ComponentService componentService;

    public BlackDuckMessageComponentVersionUpgradeGuidanceService(ComponentService componentService) {
        this.componentService = componentService;
    }

    public List<LinkableItem> requestRemediationItems(ComponentVersionView componentVersionView) throws IntegrationException {
        Optional<ComponentVersionRemediatingView> optionalRemediationInformation = componentService.getRemediationInformation(componentVersionView);
        if (optionalRemediationInformation.isPresent()) {
            ComponentVersionRemediatingView remediatingView = optionalRemediationInformation.get();

            List<LinkableItem> remediationItems = new ArrayList<>(3);

            Optional.ofNullable(remediatingView.getFixesPreviousVulnerabilities())
                .map(remediationView -> createRemediationItem(BlackDuckMessageLabels.LABEL_REMEDIATION_FIX_PREVIOUS, remediationView))
                .ifPresent(remediationItems::add);

            Optional.ofNullable(remediatingView.getLatestAfterCurrent())
                .map(remediationView -> createRemediationItem(BlackDuckMessageLabels.LABEL_REMEDIATION_LATEST, remediationView))
                .ifPresent(remediationItems::add);

            Optional.ofNullable(remediatingView.getNoVulnerabilities())
                .map(remediationView -> createRemediationItem(BlackDuckMessageLabels.LABEL_REMEDIATION_CLEAN, remediationView))
                .ifPresent(remediationItems::add);

            return remediationItems;
        }
        return List.of();
    }

    public List<LinkableItem> requestUpgradeGuidanceItems(ComponentVersionView componentVersionView) throws IntegrationException {
        Optional<ComponentVersionUpgradeGuidanceView> optionalUpgradeGuidance = componentService.getUpgradeGuidance(componentVersionView);
        if (optionalUpgradeGuidance.isPresent()) {
            ComponentVersionUpgradeGuidanceView upgradeGuidanceView = optionalUpgradeGuidance.get();

            List<LinkableItem> guidanceItems = new ArrayList<>(2);

            Optional.ofNullable(upgradeGuidanceView.getShortTerm())
                .map(CommonUpgradeGuidanceModel::fromShortTermGuidance)
                .map(guidanceModel -> createUpgradeGuidanceItem(BlackDuckMessageLabels.LABEL_GUIDANCE_SHORT_TERM, guidanceModel))
                .ifPresent(guidanceItems::add);

            Optional.ofNullable(upgradeGuidanceView.getLongTerm())
                .map(CommonUpgradeGuidanceModel::fromLongTermGuidance)
                .map(guidanceModel -> createUpgradeGuidanceItem(BlackDuckMessageLabels.LABEL_GUIDANCE_LONG_TERM, guidanceModel))
                .ifPresent(guidanceItems::add);

            return guidanceItems;
        }
        return List.of();
    }

    private LinkableItem createRemediationItem(String label, RemediatingVersionView remediatingVersionView) {
        String remediationText = createRemediationText(remediatingVersionView);
        return new LinkableItem(label, remediationText, remediatingVersionView.getComponentVersion());
    }

    private String createRemediationText(RemediatingVersionView remediatingVersionView) {
        BigDecimal vulnerabilityCount = remediatingVersionView.getVulnerabilityCount();
        String vulnerabilityCountString;
        if (vulnerabilityCount != null && vulnerabilityCount.intValue() > 0) {
            vulnerabilityCountString = vulnerabilityCount.toPlainString();
        } else {
            vulnerabilityCountString = "None";
        }
        return String.format("%s Vulnerability Count: %s", remediatingVersionView.getName(), vulnerabilityCountString);
    }

    private LinkableItem createUpgradeGuidanceItem(String label, CommonUpgradeGuidanceModel upgradeGuidanceModel) {
        String upgradeGuidanceText = createUpgradeGuidanceText(upgradeGuidanceModel);
        return new LinkableItem(label, upgradeGuidanceText, upgradeGuidanceModel.getVersion());
    }

    private String createUpgradeGuidanceText(CommonUpgradeGuidanceModel upgradeGuidanceModel) {
        UpgradeGuidanceRiskModel risk = upgradeGuidanceModel.getVulnerabilityRisk();

        List<String> riskCountStrings = new ArrayList<>(4);
        risk.getCritical()
            .map(count -> createVulnerabilityCountString("Critical", count))
            .ifPresent(riskCountStrings::add);
        risk.getHigh()
            .map(count -> createVulnerabilityCountString("High", count))
            .ifPresent(riskCountStrings::add);
        risk.getMedium()
            .map(count -> createVulnerabilityCountString("Medium", count))
            .ifPresent(riskCountStrings::add);
        risk.getLow()
            .map(count -> createVulnerabilityCountString("Low", count))
            .ifPresent(riskCountStrings::add);

        String vulnerabilitiesString;
        if (riskCountStrings.isEmpty()) {
            vulnerabilitiesString = "None";
        } else {
            vulnerabilitiesString = StringUtils.join(riskCountStrings, ", ");
        }
        return String.format("%s (Vulnerabilities: %s)", upgradeGuidanceModel.getVersionName(), vulnerabilitiesString);
    }

    private String createVulnerabilityCountString(String label, int vulnerabilityCount) {
        return String.format("%s: %d", label, vulnerabilityCount);
    }

}

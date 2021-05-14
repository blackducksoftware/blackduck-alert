/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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

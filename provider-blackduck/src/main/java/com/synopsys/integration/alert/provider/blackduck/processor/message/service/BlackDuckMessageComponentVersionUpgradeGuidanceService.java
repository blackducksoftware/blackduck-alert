/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionUpgradeGuidanceView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.VersionBomOriginView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;

public class BlackDuckMessageComponentVersionUpgradeGuidanceService {
    private final BlackDuckApiClient blackDuckApiClient;

    public BlackDuckMessageComponentVersionUpgradeGuidanceService(BlackDuckApiClient blackDuckApiClient) {
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public List<LinkableItem> requestUpgradeGuidanceItems(ProjectVersionComponentView bomComponent) throws IntegrationException {
        // TODO determine what to do with multiple origins
        Optional<UrlSingleResponse<ComponentVersionUpgradeGuidanceView>> upgradeGuidanceUrl = Optional.empty();
        Optional<VersionBomOriginView> optionalOrigin = bomComponent.getOrigins().stream().findFirst();
        if (optionalOrigin.isPresent()) {
            VersionBomOriginView origin = optionalOrigin.get();
            // FIXME
            //  upgradeGuidanceUrl = origin.metaUpgradeGuidanceLinkSafely();
        } else {
            LinkSingleResponse<ComponentVersionUpgradeGuidanceView> upgradeGuidanceViewLinkName = new LinkSingleResponse<>("upgrade-guidance", ComponentVersionUpgradeGuidanceView.class);
            upgradeGuidanceUrl = bomComponent.metaSingleResponseSafely(upgradeGuidanceViewLinkName);
        }

        if (upgradeGuidanceUrl.isPresent()) {
            ComponentVersionUpgradeGuidanceView upgradeGuidanceView = blackDuckApiClient.getResponse(upgradeGuidanceUrl.get());
            return createUpgradeGuidanceItems(upgradeGuidanceView);
        }
        return List.of();
    }

    public List<LinkableItem> requestUpgradeGuidanceItems(ComponentVersionView componentVersionView) throws IntegrationException {
        Optional<UrlSingleResponse<ComponentVersionUpgradeGuidanceView>> upgradeGuidanceUrl = componentVersionView.metaUpgradeGuidanceLinkSafely();
        if (upgradeGuidanceUrl.isPresent()) {
            ComponentVersionUpgradeGuidanceView upgradeGuidanceView = blackDuckApiClient.getResponse(upgradeGuidanceUrl.get());
            return createUpgradeGuidanceItems(upgradeGuidanceView);
        }
        return List.of();
    }

    private List<LinkableItem> createUpgradeGuidanceItems(ComponentVersionUpgradeGuidanceView upgradeGuidanceView) {
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

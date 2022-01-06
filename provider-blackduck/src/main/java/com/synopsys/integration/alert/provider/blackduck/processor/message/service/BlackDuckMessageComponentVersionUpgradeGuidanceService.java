/*
 * provider-blackduck
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.provider.blackduck.processor.message.BlackDuckMessageLabels;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionUpgradeGuidanceView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckMessageComponentVersionUpgradeGuidanceService {
    private static final String LINK_UPGRADE_GUIDANCE = "upgrade-guidance";
    private final BlackDuckApiClient blackDuckApiClient;

    public BlackDuckMessageComponentVersionUpgradeGuidanceService(BlackDuckApiClient blackDuckApiClient) {
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public ComponentUpgradeGuidance requestUpgradeGuidanceItems(ProjectVersionComponentVersionView bomComponent) throws IntegrationException {
        // TODO determine what to do with multiple origins
        Optional<UrlSingleResponse<ComponentVersionUpgradeGuidanceView>> upgradeGuidanceUrl = bomComponent.getOrigins()
                                                                                                  .stream()
                                                                                                  .findFirst()
                                                                                                  .flatMap(origin -> Optional.ofNullable(origin.getMeta()))
                                                                                                  .flatMap(this::extractFirstUpgradeGuidanceLinkSafely)
                                                                                                  .map(url -> new UrlSingleResponse<>(url, ComponentVersionUpgradeGuidanceView.class))
                                                                                                  .or(() -> extractComponentVersionUpgradeGuidanceUrl(bomComponent));
        if (upgradeGuidanceUrl.isPresent()) {
            ComponentVersionUpgradeGuidanceView upgradeGuidanceView = blackDuckApiClient.getResponse(upgradeGuidanceUrl.get());
            return createUpgradeGuidanceItems(upgradeGuidanceView);
        }
        return ComponentUpgradeGuidance.none();
    }

    public ComponentUpgradeGuidance requestUpgradeGuidanceItems(ComponentVersionView componentVersionView) throws IntegrationException {
        Optional<UrlSingleResponse<ComponentVersionUpgradeGuidanceView>> upgradeGuidanceUrl = componentVersionView.metaUpgradeGuidanceLinkSafely();
        if (upgradeGuidanceUrl.isPresent()) {
            ComponentVersionUpgradeGuidanceView upgradeGuidanceView = blackDuckApiClient.getResponse(upgradeGuidanceUrl.get());
            return createUpgradeGuidanceItems(upgradeGuidanceView);
        }
        return ComponentUpgradeGuidance.none();
    }

    private Optional<HttpUrl> extractFirstUpgradeGuidanceLinkSafely(ResourceMetadata meta) {
        return meta.getLinks()
                   .stream()
                   .filter(resourceLink -> resourceLink.getRel().equals(LINK_UPGRADE_GUIDANCE))
                   .map(ResourceLink::getHref)
                   .findFirst();
    }

    private Optional<UrlSingleResponse<ComponentVersionUpgradeGuidanceView>> extractComponentVersionUpgradeGuidanceUrl(ProjectVersionComponentVersionView bomComponent) {
        LinkSingleResponse<ComponentVersionUpgradeGuidanceView> upgradeGuidanceViewLinkName = new LinkSingleResponse<>(LINK_UPGRADE_GUIDANCE, ComponentVersionUpgradeGuidanceView.class);
        return bomComponent.metaSingleResponseSafely(upgradeGuidanceViewLinkName);
    }

    private ComponentUpgradeGuidance createUpgradeGuidanceItems(ComponentVersionUpgradeGuidanceView upgradeGuidanceView) {
        LinkableItem shortTermGuidance = Optional.ofNullable(upgradeGuidanceView.getShortTerm())
                                             .map(CommonUpgradeGuidanceModel::fromShortTermGuidance)
                                             .map(guidanceModel -> createUpgradeGuidanceItem(BlackDuckMessageLabels.LABEL_GUIDANCE_SHORT_TERM, guidanceModel))
                                             .orElse(null);
        LinkableItem longTermGuidance = Optional.ofNullable(upgradeGuidanceView.getLongTerm())
                                            .map(CommonUpgradeGuidanceModel::fromLongTermGuidance)
                                            .map(guidanceModel -> createUpgradeGuidanceItem(BlackDuckMessageLabels.LABEL_GUIDANCE_LONG_TERM, guidanceModel))
                                            .orElse(null);
        return new ComponentUpgradeGuidance(shortTermGuidance, longTermGuidance);
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

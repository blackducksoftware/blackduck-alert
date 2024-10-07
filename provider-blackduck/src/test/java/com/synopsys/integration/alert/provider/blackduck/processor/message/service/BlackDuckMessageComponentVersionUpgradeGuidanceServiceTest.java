package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.blackduck.api.core.response.LinkSingleResponse;
import com.blackduck.integration.blackduck.api.core.response.UrlSingleResponse;
import com.blackduck.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermView;
import com.blackduck.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView;
import com.blackduck.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermView;
import com.blackduck.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView;
import com.blackduck.integration.blackduck.api.generated.response.ComponentVersionUpgradeGuidanceView;
import com.blackduck.integration.blackduck.api.generated.view.ComponentVersionView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpUrl;

class BlackDuckMessageComponentVersionUpgradeGuidanceServiceTest {
    @Test
    void requestUpgradeGuidanceItemsBomTest() throws IntegrationException {
        HttpUrl httpUrl = new HttpUrl("https://fake-url");
        UrlSingleResponse<ComponentVersionUpgradeGuidanceView> expectedUrl = new UrlSingleResponse<>(httpUrl, ComponentVersionUpgradeGuidanceView.class);
        ComponentVersionUpgradeGuidanceView upgradeGuidanceView = createUpgradeGuidance(true);
        BlackDuckApiClient blackDuckApiClient = createBlackDuckApiClient(expectedUrl, upgradeGuidanceView);

        LinkSingleResponse<ComponentVersionUpgradeGuidanceView> upgradeGuidanceLink = new LinkSingleResponse<>("upgrade-guidance", ComponentVersionUpgradeGuidanceView.class);
        ProjectVersionComponentVersionView bomComponent = createBomComponent(upgradeGuidanceLink, expectedUrl);

        BlackDuckMessageComponentVersionUpgradeGuidanceService upgradeGuidanceService = new BlackDuckMessageComponentVersionUpgradeGuidanceService(blackDuckApiClient);
        ComponentUpgradeGuidance componentUpgradeGuidance = upgradeGuidanceService.requestUpgradeGuidanceItems(bomComponent);
        assertTrue(componentUpgradeGuidance.getLongTermUpgradeGuidance().isPresent());
        assertTrue(componentUpgradeGuidance.getShortTermUpgradeGuidance().isPresent());
    }

    @Test
    void requestUpgradeGuidanceItemsComponentTest() throws IntegrationException {
        HttpUrl httpUrl = new HttpUrl("https://fake-url");
        UrlSingleResponse<ComponentVersionUpgradeGuidanceView> expectedUrl = new UrlSingleResponse<>(httpUrl, ComponentVersionUpgradeGuidanceView.class);
        ComponentVersionUpgradeGuidanceView upgradeGuidanceView = createUpgradeGuidance(false);
        BlackDuckApiClient blackDuckApiClient = createBlackDuckApiClient(expectedUrl, upgradeGuidanceView);

        ComponentVersionView componentVersion = createComponentVersion(expectedUrl);

        BlackDuckMessageComponentVersionUpgradeGuidanceService upgradeGuidanceService = new BlackDuckMessageComponentVersionUpgradeGuidanceService(blackDuckApiClient);
        ComponentUpgradeGuidance componentUpgradeGuidance = upgradeGuidanceService.requestUpgradeGuidanceItems(componentVersion);
        assertTrue(componentUpgradeGuidance.getLongTermUpgradeGuidance().isPresent());
        assertFalse(componentUpgradeGuidance.getShortTermUpgradeGuidance().isPresent());
    }

    private ProjectVersionComponentVersionView createBomComponent(LinkSingleResponse<ComponentVersionUpgradeGuidanceView> upgradeGuidanceLink, UrlSingleResponse<ComponentVersionUpgradeGuidanceView> expectedUrl) {
        ProjectVersionComponentVersionView bomComponent = Mockito.mock(ProjectVersionComponentVersionView.class);
        Mockito.when(bomComponent.metaSingleResponseSafely(upgradeGuidanceLink)).thenReturn(Optional.of(expectedUrl));
        return bomComponent;
    }

    private ComponentVersionView createComponentVersion(UrlSingleResponse<ComponentVersionUpgradeGuidanceView> expectedUrl) {
        ComponentVersionView componentVersion = Mockito.mock(ComponentVersionView.class);
        Mockito.when(componentVersion.metaUpgradeGuidanceLinkSafely()).thenReturn(Optional.of(expectedUrl));
        return componentVersion;
    }

    private ComponentVersionUpgradeGuidanceView createUpgradeGuidance(boolean includeShortTerm) {
        ComponentVersionUpgradeGuidanceView upgradeGuidanceView = new ComponentVersionUpgradeGuidanceView();

        if (includeShortTerm) {
            ComponentVersionUpgradeGuidanceShortTermView shortTermView = new ComponentVersionUpgradeGuidanceShortTermView();
            ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView shortTermRiskView = new ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView();
            shortTermRiskView.setCritical(BigDecimal.ZERO);
            shortTermRiskView.setHigh(BigDecimal.ZERO);
            shortTermRiskView.setMedium(BigDecimal.ZERO);
            shortTermRiskView.setLow(BigDecimal.ZERO);

            shortTermView.setVulnerabilityRisk(shortTermRiskView);
            upgradeGuidanceView.setShortTerm(shortTermView);
        }

        ComponentVersionUpgradeGuidanceLongTermView longTermView = new ComponentVersionUpgradeGuidanceLongTermView();
        ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView longTermRiskView = new ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView();
        longTermRiskView.setCritical(BigDecimal.TEN);
        longTermRiskView.setHigh(BigDecimal.valueOf(20L));
        longTermRiskView.setMedium(BigDecimal.valueOf(30L));
        longTermRiskView.setLow(BigDecimal.valueOf(40L));

        longTermView.setVulnerabilityRisk(longTermRiskView);
        upgradeGuidanceView.setLongTerm(longTermView);

        return upgradeGuidanceView;
    }

    private BlackDuckApiClient createBlackDuckApiClient(UrlSingleResponse<ComponentVersionUpgradeGuidanceView> expectedArg, ComponentVersionUpgradeGuidanceView expectedReturn) throws IntegrationException {
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckApiClient.getResponse(expectedArg)).thenReturn(expectedReturn);
        return blackDuckApiClient;
    }

}

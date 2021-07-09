package com.synopsys.integration.alert.provider.blackduck.processor.message.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceLongTermVulnerabilityRiskView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermView;
import com.synopsys.integration.blackduck.api.generated.component.ComponentVersionUpgradeGuidanceShortTermVulnerabilityRiskView;
import com.synopsys.integration.blackduck.api.generated.response.ComponentVersionUpgradeGuidanceView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckMessageComponentVersionUpgradeGuidanceServiceTest {
    @Test
    public void requestUpgradeGuidanceItemsBomTest() throws IntegrationException {
        HttpUrl httpUrl = new HttpUrl("https://fake-url");
        UrlSingleResponse<ComponentVersionUpgradeGuidanceView> expectedUrl = new UrlSingleResponse<>(httpUrl, ComponentVersionUpgradeGuidanceView.class);
        ComponentVersionUpgradeGuidanceView upgradeGuidanceView = createUpgradeGuidance(true);
        BlackDuckApiClient blackDuckApiClient = createBlackDuckApiClient(expectedUrl, upgradeGuidanceView);

        LinkSingleResponse<ComponentVersionUpgradeGuidanceView> upgradeGuidanceLink = new LinkSingleResponse<>("upgrade-guidance", ComponentVersionUpgradeGuidanceView.class);
        ProjectVersionComponentView bomComponent = createBomComponent(upgradeGuidanceLink, expectedUrl);

        BlackDuckMessageComponentVersionUpgradeGuidanceService upgradeGuidanceService = new BlackDuckMessageComponentVersionUpgradeGuidanceService(blackDuckApiClient);
        List<LinkableItem> upgradeGuidanceItems = upgradeGuidanceService.requestUpgradeGuidanceItems(bomComponent);
        assertEquals(2, upgradeGuidanceItems.size());
    }

    @Test
    public void requestUpgradeGuidanceItemsComponentTest() throws IntegrationException {
        HttpUrl httpUrl = new HttpUrl("https://fake-url");
        UrlSingleResponse<ComponentVersionUpgradeGuidanceView> expectedUrl = new UrlSingleResponse<>(httpUrl, ComponentVersionUpgradeGuidanceView.class);
        ComponentVersionUpgradeGuidanceView upgradeGuidanceView = createUpgradeGuidance(false);
        BlackDuckApiClient blackDuckApiClient = createBlackDuckApiClient(expectedUrl, upgradeGuidanceView);

        ComponentVersionView componentVersion = createComponentVersion(expectedUrl);

        BlackDuckMessageComponentVersionUpgradeGuidanceService upgradeGuidanceService = new BlackDuckMessageComponentVersionUpgradeGuidanceService(blackDuckApiClient);
        List<LinkableItem> upgradeGuidanceItems = upgradeGuidanceService.requestUpgradeGuidanceItems(componentVersion);
        assertEquals(1, upgradeGuidanceItems.size());
    }

    private ProjectVersionComponentView createBomComponent(LinkSingleResponse<ComponentVersionUpgradeGuidanceView> upgradeGuidanceLink, UrlSingleResponse<ComponentVersionUpgradeGuidanceView> expectedUrl) {
        ProjectVersionComponentView bomComponent = Mockito.mock(ProjectVersionComponentView.class);
        Mockito.when(bomComponent.metaSingleResponseSafely(Mockito.eq(upgradeGuidanceLink))).thenReturn(Optional.of(expectedUrl));
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
        Mockito.when(blackDuckApiClient.getResponse(Mockito.eq(expectedArg))).thenReturn(expectedReturn);
        return blackDuckApiClient;
    }

}

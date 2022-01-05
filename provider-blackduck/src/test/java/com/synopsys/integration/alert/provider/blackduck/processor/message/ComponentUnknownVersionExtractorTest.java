package com.synopsys.integration.alert.provider.blackduck.processor.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckComponentVulnerabilityDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.synopsys.integration.alert.provider.blackduck.processor.model.ComponentUnknownVersionWithStatusNotificationContent;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.enumeration.ComponentUnknownVersionStatus;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class ComponentUnknownVersionExtractorTest {
    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component", "http://bomComponentUrl?q=componentOrVersionName:A%20BOM%20component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "Unknown Version");
    private static final String COMPONENT_VERSION_URL = "http://componentVersionUrl";
    private static final String LICENSE_DISPLAY = "licenseDisplay";

    private final ComponentUnknownVersionWithStatusNotificationContent componentUnknownVersionNotificationStatus = createNotificationContent();
    private ComponentUnknownVersionExtractor extractor;

    @BeforeEach
    public void init() {
        BlackDuckProviderKey providerKey = new BlackDuckProviderKey();
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
        BomComponent404Handler bomComponent404Handler = new BomComponent404Handler();

        BlackDuckPolicySeverityConverter blackDuckPolicySeverityConverter = new BlackDuckPolicySeverityConverter();

        BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator = new BlackDuckComponentVulnerabilityDetailsCreator();
        BlackDuckComponentPolicyDetailsCreatorFactory blackDuckComponentPolicyDetailsCreatorFactory = new BlackDuckComponentPolicyDetailsCreatorFactory(blackDuckPolicySeverityConverter);
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory = new BlackDuckMessageBomComponentDetailsCreatorFactory(vulnerabilityDetailsCreator, blackDuckComponentPolicyDetailsCreatorFactory);

        extractor = new ComponentUnknownVersionExtractor(
            providerKey,
            servicesFactoryCache,
            detailsCreatorFactory,
            bomComponent404Handler
        );
    }

    @Test
    public void createBomComponentDetailsTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        ProjectVersionComponentVersionView projectVersionComponentVersionView = createProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(projectVersionComponentVersionView);

        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("http://bomComponentUrl"));

        List<BomComponentDetails> bomComponentDetailsList = extractor.createBomComponentDetails(componentUnknownVersionNotificationStatus, blackDuckServicesFactory);

        assertEquals(1, bomComponentDetailsList.size());
        BomComponentDetails testBomComponentDetails = bomComponentDetailsList.get(0);
        assertEquals(COMPONENT, testBomComponentDetails.getComponent());
        assertEquals(4, testBomComponentDetails.getComponentConcerns().size());
        assertTrue(testBomComponentDetails.getComponentVersion().isPresent());
        assertEquals(LICENSE_DISPLAY, testBomComponentDetails.getLicense().getValue());
        assertEquals(UsageType.DYNAMICALLY_LINKED.prettyPrint(), testBomComponentDetails.getUsage());
        assertEquals(0, testBomComponentDetails.getAdditionalAttributes().size());

        ComponentUpgradeGuidance componentUpgradeGuidance = testBomComponentDetails.getComponentUpgradeGuidance();
        assertFalse(componentUpgradeGuidance.getLongTermUpgradeGuidance().isPresent());
        assertFalse(componentUpgradeGuidance.getShortTermUpgradeGuidance().isPresent());
    }

    @Test
    public void createBomComponentDetailsMissingBomComponentTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        Mockito.doThrow(new IntegrationRestException(HttpMethod.GET, new HttpUrl("https://google.com"), HttpStatus.NOT_FOUND.value(), "httpStatusMessageTest", "httpResponseContentTest", "IntegrationRestExceptionForAlertTest"))
            .when(blackDuckApiClient).getResponse(Mockito.any(), Mockito.any());

        List<BomComponentDetails> bomComponentDetailsList = extractor.createBomComponentDetails(componentUnknownVersionNotificationStatus, blackDuckServicesFactory);

        assertEquals(1, bomComponentDetailsList.size());
        BomComponentDetails testBomComponentDetails = bomComponentDetailsList.get(0);
        assertEquals(COMPONENT, testBomComponentDetails.getComponent());
        assertEquals(4, testBomComponentDetails.getComponentConcerns().size());
        assertFalse(testBomComponentDetails.getComponentVersion().isPresent());
        assertEquals(BlackDuckMessageLabels.VALUE_UNKNOWN_LICENSE, testBomComponentDetails.getLicense().getValue());
        assertEquals(BlackDuckMessageLabels.VALUE_UNKNOWN_USAGE, testBomComponentDetails.getUsage());
    }

    private ComponentUnknownVersionWithStatusNotificationContent createNotificationContent() {
        String projectName = "Project";
        String projectVersionName = "ProjectVersionName";
        String projectVersion = "http://projectVersionUrl";
        String componentName = COMPONENT.getValue();
        String bomComponent = "http://bomComponentUrl";

        return new ComponentUnknownVersionWithStatusNotificationContent(projectName, projectVersionName, projectVersion,
            componentName,
            bomComponent,
            COMPONENT.getUrl().orElse(null),
            1,
            "http://criticalVulnVersionURL",
            "critical vulnerability name",
            2,
            "http://criticalVulnVersionURL",
            "critical vulnerability name",
            3,
            "http://criticalVulnVersionURL",
            "critical vulnerability name",
            4,
            "http://criticalVulnVersionURL",
            "critical vulnerability name",
            ComponentUnknownVersionStatus.FOUND);
    }

    private ProjectVersionComponentVersionView createProjectVersionComponentVersionView() throws IntegrationException {
        ProjectVersionComponentVersionView projectVersionComponentVersionView = new ProjectVersionComponentVersionView();

        projectVersionComponentVersionView.setComponentName(COMPONENT.getValue());
        projectVersionComponentVersionView.setComponentVersion(COMPONENT_VERSION_URL);
        projectVersionComponentVersionView.setComponentVersionName(COMPONENT_VERSION.getValue());
        projectVersionComponentVersionView.setUsages(List.of(UsageType.DYNAMICALLY_LINKED));

        ProjectVersionComponentVersionLicensesView projectVersionComponentVersionLicensesView = new ProjectVersionComponentVersionLicensesView();
        projectVersionComponentVersionLicensesView.setLicense("http://licenseLink");
        projectVersionComponentVersionLicensesView.setLicenseDisplay(LICENSE_DISPLAY);
        projectVersionComponentVersionView.setLicenses(List.of(projectVersionComponentVersionLicensesView));

        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("http://bomComponentUrl"));
        meta.setLinks(List.of());
        projectVersionComponentVersionView.setMeta(meta);

        return projectVersionComponentVersionView;
    }

}

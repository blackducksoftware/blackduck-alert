package com.synopsys.integration.alert.provider.blackduck.processor.message;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckComponentVulnerabilityDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.component.ComponentUnknownVersionNotificationContent;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class ComponentUnknownVersionExtractorTest {
    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final String COMPONENT_VERSION_URL = "http://componentVersionUrl";
    private static final String LICENSE_DISPLAY = "licenseDisplay";

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
/*
    @Test
    public void createBomComponentDetailsTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        ProjectVersionComponentVersionView projectVersionComponentVersionView = createProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(projectVersionComponentVersionView);

        ComponentUnknownVersionNotificationUserView componentUnknownVersionNotificationUserView = new ComponentUnknownVersionNotificationUserView();
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://someUrlComponentUnknownVersionView"));
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.eq(projectVersionComponentVersionView.metaPolicyRulesLink()))).thenReturn(List.of(componentPolicyRulesView));

        List<BomComponentDetails> bomComponentDetailsList = extractor.createBomComponentDetails(componentUnknownVersionNotificationUserView, blackDuckServicesFactory);

        assertEquals(1, bomComponentDetailsList.size());
        BomComponentDetails testBomComponentDetails = bomComponentDetailsList.get(0);
        assertEquals(COMPONENT, testBomComponentDetails.getComponent());
        assertEquals(1, testBomComponentDetails.getComponentConcerns().size());
        assertTrue(testBomComponentDetails.getComponentVersion().isPresent());
        assertEquals(COMPONENT_VERSION.getValue(), testBomComponentDetails.getComponentVersion().get().getValue());
        assertEquals(LICENSE_DISPLAY, testBomComponentDetails.getLicense().getValue());
        assertEquals(UsageType.DYNAMICALLY_LINKED.prettyPrint(), testBomComponentDetails.getUsage());
        assertEquals(1, testBomComponentDetails.getAdditionalAttributes().size());

        ComponentUpgradeGuidance componentUpgradeGuidance = testBomComponentDetails.getComponentUpgradeGuidance();
        assertFalse(componentUpgradeGuidance.getLongTermUpgradeGuidance().isPresent());
        assertFalse(componentUpgradeGuidance.getShortTermUpgradeGuidance().isPresent());

        assertEquals(1, testBomComponentDetails.getRelevantPolicies().size());
        ComponentPolicy testComponentPolicy = testBomComponentDetails.getRelevantPolicies().get(0);
        assertTrue(testComponentPolicy.getCategory().isPresent());
        assertEquals(PolicyRuleCategoryType.UNCATEGORIZED.toString(), testComponentPolicy.getCategory().get());
    }

    @Test
    public void createBomComponentDetailsMissingBomComponentTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        Mockito.doThrow(new IntegrationRestException(HttpMethod.GET, new HttpUrl("https://google.com"), HttpStatus.NOT_FOUND.value(), "httpStatusMessageTest", "httpResponseContentTest", "IntegrationRestExceptionForAlertTest"))
            .when(blackDuckApiClient).getResponse(Mockito.any(), Mockito.any());

        List<BomComponentDetails> bomComponentDetailsList = extractor.createBomComponentDetails(policyOverrideUniquePolicyNotificationContent, blackDuckServicesFactory);

        assertEquals(1, bomComponentDetailsList.size());
        BomComponentDetails testBomComponentDetails = bomComponentDetailsList.get(0);
        assertEquals(COMPONENT, testBomComponentDetails.getComponent());
        assertEquals(1, testBomComponentDetails.getComponentConcerns().size());
        assertTrue(testBomComponentDetails.getComponentVersion().isPresent());
        assertEquals(COMPONENT_VERSION.getValue(), testBomComponentDetails.getComponentVersion().get().getValue());
        assertTrue(testBomComponentDetails.getRelevantPolicies().isEmpty());
        assertEquals(BlackDuckMessageLabels.VALUE_UNKNOWN_LICENSE, testBomComponentDetails.getLicense().getValue());
        assertEquals(BlackDuckMessageLabels.VALUE_UNKNOWN_USAGE, testBomComponentDetails.getUsage());
    }
 */

    private ComponentUnknownVersionNotificationContent createNotificationContent() {
        String projectName = "Project";
        String projectVersionName = "ProjectVersionName";
        String projectVersion = "http://projectVersionUrl";
        String componentName = COMPONENT.getValue();
        String componentVersionName = COMPONENT_VERSION.getValue();
        String bomComponentVersionPolicyStatus = "BomComponentVersionPolicyStatus";
        String bomComponent = "http://bomComponentUrl";

        ComponentUnknownVersionNotificationContent content = new ComponentUnknownVersionNotificationContent();
        content.setComponent(COMPONENT_VERSION_URL);
        content.setBomComponent(bomComponent);
        content.setComponentName(componentName);
        content.setProjectName(projectName);
        content.setProjectVersionName(projectVersionName);
        content.setProjectVersion(projectVersion);
        content.setCriticalVulnerabilityCount(1);
        content.setCriticalVulnerabilityName("critical vulnerability name");
        content.setCriticalVulnerabilityVersion("http://criticalVulnVersionURL");
        content.setHighVulnerabilityCount(2);
        content.setHighVulnerabilityVersionName("critical vulnerability name");
        content.setHighVulnerabilityVersion("http://criticalVulnVersionURL");
        content.setMediumVulnerabilityCount(3);
        content.setMediumVulnerabilityVersionName("critical vulnerability name");
        content.setMediumVulnerabilityVersion("http://criticalVulnVersionURL");
        content.setLowVulnerabilityCount(4);
        content.setLowVulnerabilityVersionName("critical vulnerability name");
        content.setLowVulnerabilityVersion("http://criticalVulnVersionURL");
        return content;
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
        meta.setHref(new HttpUrl("https://someUrl"));
        projectVersionComponentVersionView.setMeta(meta);

        return projectVersionComponentVersionView;
    }

}

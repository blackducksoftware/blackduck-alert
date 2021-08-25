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
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckComponentVulnerabilityDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.synopsys.integration.alert.provider.blackduck.processor.model.BomEditWithProjectNameNotificationContent;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BomEditNotificationMessageExtractorTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Provider", "A provider", "https://provider-url"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "A project", "https://project-url");
    private static final LinkableItem PROJECT_VERSION_ITEM = new LinkableItem("Project Version", "2.3.4-RC", "https://project-version-url");

    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final String COMPONENT_VERSION_URL = "http://componentVersionUrl";
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = ComponentVulnerabilities.none();
    private static final LinkableItem LICENSE = new LinkableItem("License", "A software license", "https://license-url");
    private static final String LICENSE_DISPLAY = "licenseDisplay";
    private static final String USAGE = "Some generic usage";
    private static final String ISSUES_URL = "https://issues-url";

    private final BlackDuckProviderKey providerKey = new BlackDuckProviderKey();
    private final NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
    private final BomComponent404Handler bomComponent404Handler = new BomComponent404Handler();

    private final BomEditWithProjectNameNotificationContent notificationContent = createBomEditWithProjectNameNotificationContent();
    private BomEditNotificationMessageExtractor extractor;

    @BeforeEach
    public void init() {
        BlackDuckPolicySeverityConverter blackDuckPolicySeverityConverter = new BlackDuckPolicySeverityConverter();
        BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator = new BlackDuckComponentVulnerabilityDetailsCreator();
        BlackDuckComponentPolicyDetailsCreatorFactory blackDuckComponentPolicyDetailsCreatorFactory = new BlackDuckComponentPolicyDetailsCreatorFactory(blackDuckPolicySeverityConverter);
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory = new BlackDuckMessageBomComponentDetailsCreatorFactory(vulnerabilityDetailsCreator, blackDuckComponentPolicyDetailsCreatorFactory);

        extractor = new BomEditNotificationMessageExtractor(providerKey, servicesFactoryCache, detailsCreatorFactory, bomComponent404Handler);
    }

    @Test
    public void createProjectMessageTest() {
        BomComponentDetails bomComponentDetails = new BomComponentDetails(COMPONENT, COMPONENT_VERSION, COMPONENT_VULNERABILITIES, List.of(), List.of(), LICENSE, USAGE, ComponentUpgradeGuidance.none(), List.of(), ISSUES_URL);
        ProjectMessage projectMessage = extractor.createProjectMessage(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, List.of(bomComponentDetails));

        assertEquals(MessageReason.COMPONENT_UPDATE, projectMessage.getMessageReason());
        assertTrue(projectMessage.getBomComponents().contains(bomComponentDetails));
    }

    @Test
    public void createBomComponentDetailsTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        ProjectVersionComponentVersionView projectVersionComponentVersionView = createProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(projectVersionComponentVersionView);

        List<BomComponentDetails> bomComponentDetailsList = extractor.createBomComponentDetails(notificationContent, blackDuckServicesFactory);

        assertEquals(1, bomComponentDetailsList.size());
        BomComponentDetails testBomComponentDetails = bomComponentDetailsList.get(0);
        assertEquals(COMPONENT, testBomComponentDetails.getComponent());
        assertTrue(testBomComponentDetails.getComponentVersion().isPresent());
        assertEquals(COMPONENT_VERSION.getValue(), testBomComponentDetails.getComponentVersion().get().getValue());
        assertEquals(LICENSE_DISPLAY, testBomComponentDetails.getLicense().getValue());
        assertEquals(UsageType.DYNAMICALLY_LINKED.prettyPrint(), testBomComponentDetails.getUsage());
        assertEquals(0, testBomComponentDetails.getComponentConcerns().size());
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

        List<BomComponentDetails> bomComponentDetailsList = extractor.createBomComponentDetails(notificationContent, blackDuckServicesFactory);

        assertEquals(1, bomComponentDetailsList.size());
        BomComponentDetails testBomComponentDetails = bomComponentDetailsList.get(0);
        assertEquals(COMPONENT, testBomComponentDetails.getComponent());
        assertTrue(testBomComponentDetails.getComponentVersion().isPresent());
        assertEquals(COMPONENT_VERSION.getValue(), testBomComponentDetails.getComponentVersion().get().getValue());
        assertTrue(testBomComponentDetails.getRelevantPolicies().isEmpty());
        assertEquals(0, testBomComponentDetails.getComponentConcerns().size());
        assertEquals(0, testBomComponentDetails.getAdditionalAttributes().size());

        ComponentUpgradeGuidance componentUpgradeGuidance = testBomComponentDetails.getComponentUpgradeGuidance();
        assertFalse(componentUpgradeGuidance.getLongTermUpgradeGuidance().isPresent());
        assertFalse(componentUpgradeGuidance.getShortTermUpgradeGuidance().isPresent());
    }

    private BomEditWithProjectNameNotificationContent createBomEditWithProjectNameNotificationContent() {
        BomEditNotificationContent bomEditNotificationContent = new BomEditNotificationContent();
        bomEditNotificationContent.setProjectVersion(PROJECT_VERSION_ITEM.getValue());
        bomEditNotificationContent.setBomComponent("http://bomComponentUrl");
        bomEditNotificationContent.setComponentName(COMPONENT.getValue());
        bomEditNotificationContent.setComponentVersionName(COMPONENT_VERSION.getValue());

        return new BomEditWithProjectNameNotificationContent(bomEditNotificationContent, PROJECT_ITEM.getLabel(), PROJECT_VERSION_ITEM.getLabel());
    }

    private ProjectVersionComponentVersionView createProjectVersionComponentVersionView() throws IntegrationException {
        ProjectVersionComponentVersionView projectVersionComponentVersionView = new ProjectVersionComponentVersionView();

        projectVersionComponentVersionView.setComponentName(COMPONENT.getValue());
        projectVersionComponentVersionView.setComponentVersion(COMPONENT_VERSION_URL);
        projectVersionComponentVersionView.setComponentVersionName(COMPONENT_VERSION.getValue());
        projectVersionComponentVersionView.setPolicyStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION);
        projectVersionComponentVersionView.setUsages(List.of(UsageType.DYNAMICALLY_LINKED));

        ProjectVersionComponentVersionLicensesView projectVersionComponentVersionLicensesView = new ProjectVersionComponentVersionLicensesView();
        projectVersionComponentVersionLicensesView.setLicense("http://licenseLink");
        projectVersionComponentVersionLicensesView.setLicenseDisplay(LICENSE_DISPLAY);
        projectVersionComponentVersionView.setLicenses(List.of(projectVersionComponentVersionLicensesView));

        ResourceLink resourceLink = new ResourceLink();
        resourceLink.setHref(new HttpUrl("https://someHref"));
        resourceLink.setRel("policy-rules");
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://someUrl"));
        meta.setLinks(List.of(resourceLink));
        projectVersionComponentVersionView.setMeta(meta);

        return projectVersionComponentVersionView;
    }
}

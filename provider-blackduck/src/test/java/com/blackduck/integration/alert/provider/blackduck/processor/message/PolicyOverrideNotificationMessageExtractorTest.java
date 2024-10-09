package com.blackduck.integration.alert.provider.blackduck.processor.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentPolicy;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.common.message.model.LinkableItem;
import com.blackduck.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckComponentVulnerabilityDetailsCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.blackduck.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.blackduck.integration.alert.provider.blackduck.processor.model.PolicyOverrideUniquePolicyNotificationContent;
import com.blackduck.integration.blackduck.api.core.ResourceLink;
import com.blackduck.integration.blackduck.api.core.ResourceMetadata;
import com.blackduck.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleCategoryType;
import com.blackduck.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.blackduck.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.blackduck.integration.blackduck.api.generated.enumeration.UsageType;
import com.blackduck.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.blackduck.integration.blackduck.api.generated.view.PolicyRuleView;
import com.blackduck.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.blackduck.integration.blackduck.api.manual.component.PolicyInfo;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.HttpMethod;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.exception.IntegrationRestException;

class PolicyOverrideNotificationMessageExtractorTest {
    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final String COMPONENT_VERSION_URL = "http://componentVersionUrl";
    private static final ComponentPolicy COMPONENT_POLICY = new ComponentPolicy("policyName", ComponentConcernSeverity.BLOCKER, true, false, "A Policy Description", "category");
    private static final String LICENSE_DISPLAY = "licenseDisplay";

    private final PolicyOverrideUniquePolicyNotificationContent policyOverrideUniquePolicyNotificationContent = createPolicyOverrideUniquePolicyNotificationContent();
    private PolicyOverrideNotificationMessageExtractor extractor;

    @BeforeEach
    public void init() {
        BlackDuckProviderKey providerKey = new BlackDuckProviderKey();
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
        BomComponent404Handler bomComponent404Handler = new BomComponent404Handler();

        BlackDuckPolicySeverityConverter blackDuckPolicySeverityConverter = new BlackDuckPolicySeverityConverter();
        BlackDuckPolicyComponentConcernCreator blackDuckPolicyComponentConcernCreator = new BlackDuckPolicyComponentConcernCreator(blackDuckPolicySeverityConverter);

        BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator = new BlackDuckComponentVulnerabilityDetailsCreator();
        BlackDuckComponentPolicyDetailsCreatorFactory blackDuckComponentPolicyDetailsCreatorFactory = new BlackDuckComponentPolicyDetailsCreatorFactory(blackDuckPolicySeverityConverter);
        BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory = new BlackDuckMessageBomComponentDetailsCreatorFactory(vulnerabilityDetailsCreator, blackDuckComponentPolicyDetailsCreatorFactory);

        extractor = new PolicyOverrideNotificationMessageExtractor(
            providerKey,
            servicesFactoryCache,
            blackDuckPolicyComponentConcernCreator,
            detailsCreatorFactory,
            bomComponent404Handler
        );
    }

    @Test
    void createBomComponentDetailsTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        ProjectVersionComponentVersionView projectVersionComponentVersionView = createProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(projectVersionComponentVersionView);

        ComponentPolicyRulesView componentPolicyRulesView = new ComponentPolicyRulesView();
        ResourceMetadata meta = new ResourceMetadata();
        meta.setHref(new HttpUrl("https://someUrlPolicyRuleView"));
        componentPolicyRulesView.setMeta(meta);
        componentPolicyRulesView.setName(COMPONENT_POLICY.getPolicyName());
        componentPolicyRulesView.setSeverity(PolicyRuleSeverityType.BLOCKER);
        componentPolicyRulesView.setPolicyApprovalStatus(ProjectVersionComponentPolicyStatusType.IN_VIOLATION_OVERRIDDEN);
        Mockito.when(blackDuckApiClient.getAllResponses(projectVersionComponentVersionView.metaPolicyRulesLink())).thenReturn(List.of(componentPolicyRulesView));

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setCategory(PolicyRuleCategoryType.UNCATEGORIZED);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.eq(componentPolicyRulesView.getHref()), Mockito.any())).thenReturn(policyRuleView);

        List<BomComponentDetails> bomComponentDetailsList = extractor.createBomComponentDetails(policyOverrideUniquePolicyNotificationContent, blackDuckServicesFactory);

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
    void createBomComponentDetailsMissingBomComponentTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        Mockito.doThrow(new IntegrationRestException(
                HttpMethod.GET,
                new HttpUrl("https://google.com"),
                HttpStatus.NOT_FOUND.value(),
                "httpStatusMessageTest",
                "httpResponseContentTest",
                "IntegrationRestExceptionForAlertTest"
            ))
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

    private PolicyOverrideUniquePolicyNotificationContent createPolicyOverrideUniquePolicyNotificationContent() {
        String projectName = "Project";
        String projectVersionName = "ProjectVersionName";
        String projectVersion = "http://projectVersionUrl";
        String componentName = COMPONENT.getValue();
        String componentVersionName = COMPONENT_VERSION.getValue();
        String firstName = "firstName";
        String lastName = "lastName";
        String policy = "http://policyUrl";
        String bomComponentVersionPolicyStatus = "BomComponentVersionPolicyStatus";
        String bomComponent = "http://bomComponentUrl";

        PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setPolicyName(COMPONENT_POLICY.getPolicyName());

        return new PolicyOverrideUniquePolicyNotificationContent(
            projectName,
            projectVersionName,
            projectVersion,
            componentName,
            componentVersionName,
            firstName,
            lastName,
            policyInfo,
            policy,
            bomComponentVersionPolicyStatus,
            bomComponent
        );
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

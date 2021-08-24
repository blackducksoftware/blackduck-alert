package com.synopsys.integration.alert.provider.blackduck.processor.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcern;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentConcernSeverity;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentPolicy;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckComponentVulnerabilityDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckComponentPolicyDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.synopsys.integration.alert.provider.blackduck.processor.model.PolicyOverrideUniquePolicyNotificationContent;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentVersionLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleCategoryType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicyRuleSeverityType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentPolicyStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentPolicyRulesView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class PolicyOverrideNotificationMessageExtractorTest {
    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final String COMPONENT_VERSION_URL = "http://componentVersionUrl";
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = ComponentVulnerabilities.none();
    private static final ComponentPolicy COMPONENT_POLICY = new ComponentPolicy("policyName", ComponentConcernSeverity.BLOCKER, true, false, "A Policy Description", "category");
    private static final LinkableItem LICENSE = new LinkableItem("License", "A software license", "https://license-url");
    private static final String USAGE = "Some generic usage";
    private static final String ISSUES_URL = "https://issues-url";

    private final BomComponentDetails bomComponentDetails = createBomComponentDetails();

    private final BlackDuckProviderKey providerKey = new BlackDuckProviderKey();
    private final NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
    private final BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory = Mockito.mock(BlackDuckMessageBomComponentDetailsCreatorFactory.class);
    private final BomComponent404Handler bomComponent404Handler = new BomComponent404Handler();

    private final BlackDuckPolicySeverityConverter blackDuckPolicySeverityConverter = new BlackDuckPolicySeverityConverter();
    private final BlackDuckPolicyComponentConcernCreator blackDuckPolicyComponentConcernCreator = new BlackDuckPolicyComponentConcernCreator(blackDuckPolicySeverityConverter);

    private final PolicyOverrideUniquePolicyNotificationContent policyOverrideUniquePolicyNotificationContent = createPolicyOverrideUniquePolicyNotificationContent();
    private PolicyOverrideNotificationMessageExtractor policyOverrideNotificationMessageExtractor;

    @BeforeEach
    public void init() {
        policyOverrideNotificationMessageExtractor = new PolicyOverrideNotificationMessageExtractor(
            providerKey,
            servicesFactoryCache,
            blackDuckPolicyComponentConcernCreator,
            detailsCreatorFactory,
            bomComponent404Handler
        );
    }

    @Test
    public void createBomComponentDetailsTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactoryWithMocks();

        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = Mockito.mock(BlackDuckMessageBomComponentDetailsCreator.class);
        Mockito.when(detailsCreatorFactory.createBomComponentDetailsCreator(Mockito.any())).thenReturn(bomComponentDetailsCreator);
        Mockito.when(bomComponentDetailsCreator.createBomComponentDetails(Mockito.any(), (ComponentConcern) Mockito.any(), Mockito.eq(ComponentUpgradeGuidance.none()), Mockito.anyList())).thenReturn(bomComponentDetails);

        List<BomComponentDetails> bomComponentDetailsList = policyOverrideNotificationMessageExtractor.createBomComponentDetails(policyOverrideUniquePolicyNotificationContent, blackDuckServicesFactory);
        assertTrue(bomComponentDetailsList.contains(bomComponentDetails));
    }

    @Test
    public void createBomComponentDetailsREVISITEDTest() throws IntegrationException {
        //TODO: Perhaps we can improve this test with a concrete implementation of the BlackDuckMessageBomComponentDetailsCreator
        //  we already have a policySeverityConverter, we can use that here
        BlackDuckComponentVulnerabilityDetailsCreator vulnerabilityDetailsCreator = new BlackDuckComponentVulnerabilityDetailsCreator();
        BlackDuckComponentPolicyDetailsCreatorFactory blackDuckComponentPolicyDetailsCreatorFactory = new BlackDuckComponentPolicyDetailsCreatorFactory(blackDuckPolicySeverityConverter);
        BlackDuckMessageBomComponentDetailsCreatorFactory newDetailsCreatorFactory = new BlackDuckMessageBomComponentDetailsCreatorFactory(vulnerabilityDetailsCreator, blackDuckComponentPolicyDetailsCreatorFactory);

        PolicyOverrideNotificationMessageExtractor extractor = new PolicyOverrideNotificationMessageExtractor(
            providerKey,
            servicesFactoryCache,
            blackDuckPolicyComponentConcernCreator,
            newDetailsCreatorFactory,
            bomComponent404Handler
        );

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
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.eq(projectVersionComponentVersionView.metaPolicyRulesLink()))).thenReturn(List.of(componentPolicyRulesView));

        PolicyRuleView policyRuleView = new PolicyRuleView();
        policyRuleView.setCategory(PolicyRuleCategoryType.UNCATEGORIZED);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.eq(componentPolicyRulesView.getHref()), Mockito.any())).thenReturn(policyRuleView);

        List<BomComponentDetails> bomComponentDetailsList = extractor.createBomComponentDetails(policyOverrideUniquePolicyNotificationContent, blackDuckServicesFactory);

        assertEquals(1, bomComponentDetailsList.size());
        BomComponentDetails bomComponentDetails = bomComponentDetailsList.get(0);
        assertEquals(COMPONENT, bomComponentDetails.getComponent());
        assertEquals(1, bomComponentDetails.getComponentConcerns().size());
        assertTrue(bomComponentDetails.getComponentVersion().isPresent());
        assertEquals(COMPONENT_VERSION.getValue(), bomComponentDetails.getComponentVersion().get().getValue());
        assertEquals("licenseDisplay", bomComponentDetails.getLicense().getValue());
        assertEquals(UsageType.DYNAMICALLY_LINKED.prettyPrint(), bomComponentDetails.getUsage());
        assertEquals(1, bomComponentDetails.getAdditionalAttributes().size());

        assertEquals(1, bomComponentDetails.getRelevantPolicies().size());
        ComponentPolicy testComponentPolicy = bomComponentDetails.getRelevantPolicies().get(0);
        assertTrue(testComponentPolicy.getCategory().isPresent());
        assertEquals(PolicyRuleCategoryType.UNCATEGORIZED.toString(), testComponentPolicy.getCategory().get());
    }

    @Test
    public void createBomComponentDetailsExceptionTest() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactoryWithMocks();

        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = Mockito.mock(BlackDuckMessageBomComponentDetailsCreator.class);
        Mockito.when(detailsCreatorFactory.createBomComponentDetailsCreator(Mockito.any())).thenReturn(bomComponentDetailsCreator);
        Mockito.doThrow(new IntegrationRestException(HttpMethod.GET, new HttpUrl("https://google.com"), HttpStatus.NOT_FOUND.value(), "httpStatusMessageTest", "httpResponseContentTest", "IntegrationRestExceptionForAlertTest"))
            .when(bomComponentDetailsCreator).createBomComponentDetails(Mockito.any(), (ComponentConcern) Mockito.any(), Mockito.any(), Mockito.anyList());

        Mockito.when(bomComponentDetailsCreator.createMissingBomComponentDetails(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.any(), Mockito.anyList()))
            .thenReturn(bomComponentDetails);

        List<BomComponentDetails> bomComponentDetailsList = policyOverrideNotificationMessageExtractor.createBomComponentDetails(policyOverrideUniquePolicyNotificationContent, blackDuckServicesFactory);
        assertTrue(bomComponentDetailsList.contains(bomComponentDetails));
    }

    private PolicyOverrideUniquePolicyNotificationContent createPolicyOverrideUniquePolicyNotificationContent() {
        String projectName = "Project";
        String projectVersionName = "ProjectVersionName";
        String projectVersion = "http://projectVersionUrl";
        String componentName = "ComponentName";
        String componentVersionName = "ComponentVersionName";
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
        projectVersionComponentVersionLicensesView.setLicenseDisplay("licenseDisplay");
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

    private BlackDuckServicesFactory createBlackDuckServicesFactoryWithMocks() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        ProjectVersionComponentVersionView projectVersionComponentVersionView = new ProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(projectVersionComponentVersionView);

        return blackDuckServicesFactory;
    }

    private BomComponentDetails createBomComponentDetails() {
        return new BomComponentDetails(
            COMPONENT,
            COMPONENT_VERSION,
            COMPONENT_VULNERABILITIES,
            List.of(COMPONENT_POLICY),
            List.of(),
            LICENSE,
            USAGE,
            ComponentUpgradeGuidance.none(),
            List.of(),
            ISSUES_URL
        );
    }

}

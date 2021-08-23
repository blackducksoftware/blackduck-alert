package com.synopsys.integration.alert.provider.blackduck.processor.message;

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
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicyComponentConcernCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.policy.BlackDuckPolicySeverityConverter;
import com.synopsys.integration.alert.provider.blackduck.processor.model.PolicyOverrideUniquePolicyNotificationContent;
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
        //TODO: set the policy info
        PolicyInfo policyInfo = new PolicyInfo();
        String policy = "http://policyUrl";
        String bomComponentVersionPolicyStatus = "BomComponentVersionPolicyStatus";
        String bomComponent = "http://bomComponentUrl";

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

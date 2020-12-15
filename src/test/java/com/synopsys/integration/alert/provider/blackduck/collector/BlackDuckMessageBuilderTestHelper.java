package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.List;
import java.util.Optional;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.collector.builder.BlackDuckIssueTrackerCallbackUtility;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionExpressionsView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionComponentLicensesView;
import com.synopsys.integration.blackduck.api.generated.enumeration.UsageType;
import com.synopsys.integration.blackduck.api.generated.enumeration.VulnerabilitySeverityType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectGetService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckMessageBuilderTestHelper {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckMessageBuilderTestHelper.class);

    public static BlackDuckProperties mockProperties(BlackDuckServicesFactory mockServicesFactory) {
        BlackDuckProperties mockProperties = Mockito.mock(BlackDuckProperties.class);
        BlackDuckHttpClient mockHttpClient = mockHttpClient();

        Mockito.when(mockProperties.createBlackDuckHttpClientAndLogErrors(Mockito.any())).thenReturn(Optional.of(mockHttpClient));
        Mockito.when(mockProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(mockServicesFactory);
        Mockito.when(mockProperties.getBlackDuckTimeout()).thenReturn(120);
        return mockProperties;
    }

    public static BlackDuckHttpClient mockHttpClient() {
        return Mockito.mock(BlackDuckHttpClient.class);
    }

    public static BlackDuckServicesFactory mockServicesFactory(BlackDuckApiClient blackDuckApiClient, ProjectService projectService) {
        BlackDuckServicesFactory mockServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);

        Mockito.when(mockServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);
        Mockito.when(mockServicesFactory.createProjectService()).thenReturn(projectService);

        return mockServicesFactory;
    }

    public static BlackDuckApiClient mockBlackDuckApiClient() {
        BlackDuckApiClient mockBlackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        try {
            String projectUrl1 = "https://a-hub-server.blackduck.com/api/projects/d9205017-4630-4f0c-8127-170e1db03d6f";
            String projectVersion1Href = "https://a-hub-server.blackduck.com/api/projects/d9205017-4630-4f0c-8127-170e1db03d6f/versions/ec2a759d-e27d-4445-adb2-3176f8a78d24";
            mockProjectVersion(projectUrl1, projectVersion1Href, mockBlackDuckApiClient);

            String projectUrl2 = "https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c";
            String projectVersion2Href = "https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c/versions/6c39d4f1-713d-4702-b9c8-e964e6ec932c";
            mockProjectVersion(projectUrl2, projectVersion2Href, mockBlackDuckApiClient);

            String bomComponentUri1 = "https://a-hub-server.blackduck.com/api/projects/d9205017-4630-4f0c-8127-170e1db03d6f/versions/ec2a759d-e27d-4445-adb2-3176f8a78d24/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/versions/3ef95202-5b60-4a62-ab07-02740212fd96";
            mockBomComponent(bomComponentUri1, mockBlackDuckApiClient);

            String bomComponentUri2 = "https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c/versions/6c39d4f1-713d-4702-b9c8-e964e6ec932c/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/versions/3ef95202-5b60-4a62-ab07-02740212fd96";
            mockBomComponent(bomComponentUri2, mockBlackDuckApiClient);

            String bomComponentUri3 = "https://a-hub-server.blackduck.com/api/projects/b9df44d3-8a80-4939-b4ed-b7258452d030/versions/49d2caf0-4ca0-4bba-bab0-69493b834db7/components/e5a73809-0bf4-4b5c-985a-121525d0adca";
            mockBomComponent(bomComponentUri3, mockBlackDuckApiClient);

            String componentVersionUri = "https://a-hub-server.blackduck.com/api/components/7792be90-bfd2-42d7-ae19-66e051978675/versions/5a01d0b3-a6c4-469a-b9c8-c5769cffae78";
            ComponentVersionView componentVersionView = new ComponentVersionView();
            componentVersionView.setMeta(new ResourceMetadata());
            HttpUrl componentVersionUriHttpUrl = new HttpUrl(componentVersionUri);
            componentVersionView.getMeta().setHref(componentVersionUriHttpUrl);
            Mockito.when(mockBlackDuckApiClient.getResponse(Mockito.eq(componentVersionUriHttpUrl), Mockito.eq(ComponentVersionView.class))).thenReturn(componentVersionView);

            String policyRuleUri1 = "https://a-hub-server.blackduck.com/api/policy-rules/0000001-0001-0001-0001-000000000001";
            mockPolicyRule(policyRuleUri1, mockBlackDuckApiClient);

            String policyRuleUri2 = "https://a-hub-server.blackduck.com/api/policy-rules/0000002-0002-0002-0002-000000000002";
            mockPolicyRule(policyRuleUri2, mockBlackDuckApiClient);

            String policyRuleUri3 = "https://a-hub-server.blackduck.com/api/policy-rules/0000003-0003-0003-0003-000000000003";
            mockPolicyRule(policyRuleUri3, mockBlackDuckApiClient);

            String policyRuleUri4 = "https://a-hub-server.blackduck.com/api/policy-rules/0000004-0004-0004-0004-000000000004";
            mockPolicyRule(policyRuleUri4, mockBlackDuckApiClient);

            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_1, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_2, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_3, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_4, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_5, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_6, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_7, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_8, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_9, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_10, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_11, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_12, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_13, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_BDSA_4, mockBlackDuckApiClient, VulnerabilitySeverityType.HIGH);

        } catch (IntegrationException ignored) {
            logger.error(ignored.getMessage(), ignored);
        }

        return mockBlackDuckApiClient;
    }

    public static ProjectService mockProjectService(BlackDuckApiClient blackDuckApiClient) {
        return new ProjectService(blackDuckApiClient, mockBlackDuckRequestFactory(), mockLogger(), Mockito.mock(ProjectGetService.class));
    }

    public static BlackDuckRequestFactory mockBlackDuckRequestFactory() {
        return new BlackDuckRequestFactory();
    }

    public static IntLogger mockLogger() {
        return new PrintStreamIntLogger(System.out, LogLevel.ERROR);
    }

    private static void mockBomComponent(String bomComponentUri, BlackDuckApiClient blackDuckApiClient) {
        try {
            ProjectVersionComponentLicensesView projectVersionComponentLicensesView = new ProjectVersionComponentLicensesView();
            projectVersionComponentLicensesView.setLicenseDisplay("Test License");

            ProjectVersionComponentView versionBomComponentView = new ProjectVersionComponentView();
            versionBomComponentView.setMeta(new ResourceMetadata());
            HttpUrl bomComponentUriHttpUrl = new HttpUrl(bomComponentUri);
            versionBomComponentView.getMeta().setHref(bomComponentUriHttpUrl);
            ResourceLink componentIssueResourceLink = new ResourceLink();
            componentIssueResourceLink.setRel(BlackDuckIssueTrackerCallbackUtility.COMPONENT_ISSUES_LINK_NAME);
            componentIssueResourceLink.setHref(new HttpUrl(bomComponentUri + "/" + BlackDuckIssueTrackerCallbackUtility.COMPONENT_ISSUES_LINK_NAME));
            versionBomComponentView.getMeta().setLinks(List.of(componentIssueResourceLink));
            versionBomComponentView.setLicenses(List.of(projectVersionComponentLicensesView));
            versionBomComponentView.setUsages(List.of(UsageType.DYNAMICALLY_LINKED));
            ArgumentMatcher<HttpUrl> httpUrlArgumentMatcher1 = new ArgumentMatcher<>() {
                @Override
                public boolean matches(HttpUrl httpUrl) {
                    if (null == httpUrl) {
                        return false;
                    }
                    return httpUrl.string().equals(bomComponentUri);
                }
            };
            Mockito.when(blackDuckApiClient.getResponse(Mockito.argThat(httpUrlArgumentMatcher1), Mockito.eq(ProjectVersionComponentView.class))).thenReturn(versionBomComponentView);
        } catch (IntegrationException ignored) {
            logger.error(ignored.getMessage(), ignored);
        }
    }

    private static void mockPolicyRule(String policyRuleHref, BlackDuckApiClient blackDuckApiClient) {
        try {
            PolicyRuleView policyRuleView = new PolicyRuleView();
            policyRuleView.setMeta(new ResourceMetadata());
            HttpUrl policyRuleURL = new HttpUrl(policyRuleHref);
            policyRuleView.getMeta().setHref(policyRuleURL);
            PolicyRuleExpressionView expressionView = new PolicyRuleExpressionView();
            PolicyRuleExpressionExpressionsView expressionExpressionsView = new PolicyRuleExpressionExpressionsView();
            expressionExpressionsView.setName("Test Expression");
            expressionView.setExpressions(List.of(expressionExpressionsView));
            policyRuleView.setExpression(expressionView);
            Mockito.when(blackDuckApiClient.getResponse(Mockito.eq(policyRuleURL), Mockito.eq(PolicyRuleView.class))).thenReturn(policyRuleView);
        } catch (IntegrationException ignored) {
            logger.error(ignored.getMessage(), ignored);
        }
    }

    private static void mockProjectVersion(String projectHref, String projectVersionHref, BlackDuckApiClient blackDuckApiClient) {
        try {
            ProjectVersionView projectVersionView = new ProjectVersionView();
            projectVersionView.setMeta(new ResourceMetadata());
            HttpUrl projectHrefHttpUrl = new HttpUrl(projectVersionHref);
            projectVersionView.getMeta().setHref(projectHrefHttpUrl);
            Mockito.when(blackDuckApiClient.getResponse(Mockito.eq(projectHrefHttpUrl), Mockito.eq(ProjectVersionView.class))).thenReturn(projectVersionView);

            ResourceLink resourceLink1 = new ResourceLink();
            resourceLink1.setHref(new HttpUrl(projectHref));
            resourceLink1.setRel(ProjectVersionView.PROJECT_LINK);

            ResourceLink resourceLink2 = new ResourceLink();
            resourceLink2.setHref(new HttpUrl(projectHref));
            resourceLink2.setRel(ProjectVersionView.VULNERABLE_COMPONENTS_LINK);

            ResourceLink resourceLink3 = new ResourceLink();
            resourceLink3.setHref(new HttpUrl(projectHref));
            resourceLink3.setRel(ProjectVersionView.COMPONENTS_LINK);

            projectVersionView.getMeta().setLinks(List.of(resourceLink1, resourceLink2, resourceLink3));
            Mockito.when(blackDuckApiClient.getResponse(Mockito.eq(projectHrefHttpUrl), Mockito.eq(ProjectVersionView.class))).thenReturn(projectVersionView);
        } catch (IntegrationException ignored) {
            logger.error(ignored.getMessage(), ignored);
        }
    }

    private static void mockVulnSingleResponse(String uri, BlackDuckApiClient blackDuckApiClient, VulnerabilitySeverityType severity) {
        VulnerabilityView vulnerabilityView = new VulnerabilityView();
        vulnerabilityView.setSeverity(severity);
        vulnerabilityView.setMeta(new ResourceMetadata());
        try {
            HttpUrl httpUrl = new HttpUrl(uri);
            vulnerabilityView.getMeta().setHref(httpUrl);
            Mockito.when(blackDuckApiClient.getResponse(Mockito.eq(httpUrl), Mockito.eq(VulnerabilityView.class))).thenReturn(vulnerabilityView);
        } catch (IntegrationException ignored) {
            logger.error(ignored.getMessage(), ignored);
        }
    }

}

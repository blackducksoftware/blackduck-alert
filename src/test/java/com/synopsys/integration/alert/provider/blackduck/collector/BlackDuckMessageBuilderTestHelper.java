package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.Optional;

import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.api.manual.component.ResourceMetadata;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectGetService;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class BlackDuckMessageBuilderTestHelper {
    public static BlackDuckProperties mockProperties() {
        BlackDuckProperties mockProperties = Mockito.mock(BlackDuckProperties.class);
        BlackDuckHttpClient mockHttpClient = mockHttpClient();
        BlackDuckServicesFactory mockServicesFactory = mockServicesFactory();

        Mockito.when(mockProperties.createBlackDuckHttpClientAndLogErrors(Mockito.any())).thenReturn(Optional.of(mockHttpClient));
        Mockito.when(mockProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(mockServicesFactory);
        Mockito.when(mockProperties.getBlackDuckTimeout()).thenReturn(120);
        return mockProperties;
    }

    public static BlackDuckHttpClient mockHttpClient() {
        return Mockito.mock(BlackDuckHttpClient.class);
    }

    public static BlackDuckServicesFactory mockServicesFactory() {
        BlackDuckServicesFactory mockServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckService blackDuckService = mockBlackDuckService();
        ProjectService projectService = mockProjectService();
        BlackDuckBucketService bucketService = mockBucketService();

        Mockito.when(mockServicesFactory.createBlackDuckService()).thenReturn(blackDuckService);
        Mockito.when(mockServicesFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(mockServicesFactory.createBlackDuckBucketService()).thenReturn(bucketService);

        return mockServicesFactory;
    }

    public static BlackDuckService mockBlackDuckService() {
        BlackDuckService mockBlackDuckService = Mockito.mock(BlackDuckService.class);
        try {
            String project1Href = "https://a-hub-server.blackduck.com/api/projects/d9205017-4630-4f0c-8127-170e1db03d6f/versions/ec2a759d-e27d-4445-adb2-3176f8a78d24";
            ProjectVersionView projectVersionView1 = new ProjectVersionView();
            projectVersionView1.setMeta(new ResourceMetadata());
            projectVersionView1.getMeta().setHref(project1Href);
            UriSingleResponse mockProjectSingleResponse1 = new UriSingleResponse(project1Href, ProjectVersionView.class);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(mockProjectSingleResponse1))).thenReturn(projectVersionView1);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(project1Href), Mockito.any(ProjectVersionView.class.getClass()))).thenReturn(projectVersionView1);

            String project2Href = "https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c/versions/6c39d4f1-713d-4702-b9c8-e964e6ec932c";
            ProjectVersionView projectVersionView2 = new ProjectVersionView();
            projectVersionView1.setMeta(new ResourceMetadata());
            projectVersionView1.getMeta().setHref(project2Href);
            UriSingleResponse mockProjectSingleResponse2 = new UriSingleResponse(project2Href, ProjectVersionView.class);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(mockProjectSingleResponse2))).thenReturn(projectVersionView2);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(project2Href), Mockito.any(ProjectVersionView.class.getClass()))).thenReturn(projectVersionView2);

            String bomComponentUri = "https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c/versions/6c39d4f1-713d-4702-b9c8-e964e6ec932c/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/versions/3ef95202-5b60-4a62-ab07-02740212fd96";
            ProjectVersionComponentView versionBomComponentView = new ProjectVersionComponentView();
            versionBomComponentView.setMeta(new ResourceMetadata());
            versionBomComponentView.getMeta().setHref(project2Href);
            UriSingleResponse mockComponentSingleResponse = new UriSingleResponse(bomComponentUri, ProjectVersionView.class);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(mockComponentSingleResponse))).thenReturn(versionBomComponentView);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(bomComponentUri), Mockito.any(ProjectVersionView.class.getClass()))).thenReturn(versionBomComponentView);

            String componentVersionUri = "https://a-hub-server.blackduck.com/api/components/7792be90-bfd2-42d7-ae19-66e051978675/versions/5a01d0b3-a6c4-469a-b9c8-c5769cffae78";
            ComponentVersionView componentVersionView = new ComponentVersionView();
            componentVersionView.setMeta(new ResourceMetadata());
            componentVersionView.getMeta().setHref(componentVersionUri);
            UriSingleResponse mockComponentVersionSingleResponse = new UriSingleResponse(componentVersionUri, ComponentVersionView.class);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(mockComponentVersionSingleResponse))).thenReturn(componentVersionView);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(componentVersionUri), Mockito.any(ComponentVersionView.class.getClass()))).thenReturn(componentVersionView);
            Mockito.when(mockBlackDuckService.getResponse(componentVersionUri, ComponentVersionView.class)).thenReturn(componentVersionView);

            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_1, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_2, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_3, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_4, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_5, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_6, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_7, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_8, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_9, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_10, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_11, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_12, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_CVE_13, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
            mockVulnSingleResponse(VulnerabilityTestConstants.VULNERABILITY_URL_BDSA_4, mockBlackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType.HIGH);
        } catch (IntegrationException ignored) {
        }

        return mockBlackDuckService;
    }

    public static ProjectService mockProjectService() {
        return new ProjectService(mockBlackDuckService(), mockLogger(), Mockito.mock(ProjectGetService.class));
    }

    public static BlackDuckBucketService mockBucketService() {
        return new BlackDuckBucketService(mockBlackDuckService(), mockLogger());
    }

    public static IntLogger mockLogger() {
        return new PrintStreamIntLogger(System.out, LogLevel.ERROR);
    }

    private static void mockVulnSingleResponse(String uri, BlackDuckService blackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType severity) {
        VulnerabilityView vulnerabilityView = new VulnerabilityView();
        vulnerabilityView.setSeverity(severity);
        vulnerabilityView.setMeta(new ResourceMetadata());
        vulnerabilityView.getMeta().setHref(uri);
        UriSingleResponse mockVulnSingleResponse = new UriSingleResponse(uri, VulnerabilityView.class);
        try {
            Mockito.when(blackDuckService.getResponse(Mockito.eq(mockVulnSingleResponse))).thenReturn(vulnerabilityView);
        } catch (IntegrationException ignored) {
        }
    }

}

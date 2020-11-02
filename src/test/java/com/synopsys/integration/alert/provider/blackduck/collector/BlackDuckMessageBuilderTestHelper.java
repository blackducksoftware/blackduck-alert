package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.List;
import java.util.Optional;

import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.http.RequestFactory;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectGetService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.util.NoThreadExecutorService;

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

        Mockito.when(mockServicesFactory.getBlackDuckService()).thenReturn(blackDuckService);
        Mockito.when(mockServicesFactory.createProjectService()).thenReturn(projectService);
        Mockito.when(mockServicesFactory.createBlackDuckBucketService()).thenReturn(bucketService);

        return mockServicesFactory;
    }

    public static BlackDuckService mockBlackDuckService() {
        BlackDuckService mockBlackDuckService = Mockito.mock(BlackDuckService.class);
        try {
            String projectVersion1Href = "https://a-hub-server.blackduck.com/api/projects/d9205017-4630-4f0c-8127-170e1db03d6f/versions/ec2a759d-e27d-4445-adb2-3176f8a78d24";
            ProjectVersionView projectVersionView1 = new ProjectVersionView();
            projectVersionView1.setMeta(new ResourceMetadata());
            HttpUrl project1HrefHttpUrl = new HttpUrl(projectVersion1Href);
            projectVersionView1.getMeta().setHref(project1HrefHttpUrl);
            ResourceLink resourceLink1 = new ResourceLink();
            String projectUrl1 = "https://a-hub-server.blackduck.com/api/projects/d9205017-4630-4f0c-8127-170e1db03d6f";
            resourceLink1.setHref(new HttpUrl(projectUrl1));
            resourceLink1.setRel(ProjectVersionView.PROJECT_LINK);
            projectVersionView1.getMeta().setLinks(List.of(resourceLink1));
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(project1HrefHttpUrl), Mockito.eq(ProjectVersionView.class))).thenReturn(projectVersionView1);

            String projectVersion2Href = "https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c/versions/6c39d4f1-713d-4702-b9c8-e964e6ec932c";
            ProjectVersionView projectVersionView2 = new ProjectVersionView();
            projectVersionView2.setMeta(new ResourceMetadata());
            HttpUrl project2HrefHttpURl = new HttpUrl(projectVersion2Href);
            projectVersionView1.getMeta().setHref(project2HrefHttpURl);
            ResourceLink resourceLink2 = new ResourceLink();
            String projectUrl2 = "https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c";
            resourceLink2.setHref(new HttpUrl(projectUrl2));
            resourceLink2.setRel(ProjectVersionView.PROJECT_LINK);
            projectVersionView2.getMeta().setLinks(List.of(resourceLink2));
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(project2HrefHttpURl), Mockito.eq(ProjectVersionView.class))).thenReturn(projectVersionView2);

            String bomComponentUri = "https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c/versions/6c39d4f1-713d-4702-b9c8-e964e6ec932c/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/versions/3ef95202-5b60-4a62-ab07-02740212fd96";
            ProjectVersionComponentView versionBomComponentView = new ProjectVersionComponentView();
            versionBomComponentView.setMeta(new ResourceMetadata());
            HttpUrl bomComponentUriHttpUrl = new HttpUrl(bomComponentUri);
            versionBomComponentView.getMeta().setHref(bomComponentUriHttpUrl);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(bomComponentUriHttpUrl), Mockito.eq(ProjectVersionComponentView.class))).thenReturn(versionBomComponentView);

            String componentVersionUri = "https://a-hub-server.blackduck.com/api/components/7792be90-bfd2-42d7-ae19-66e051978675/versions/5a01d0b3-a6c4-469a-b9c8-c5769cffae78";
            ComponentVersionView componentVersionView = new ComponentVersionView();
            componentVersionView.setMeta(new ResourceMetadata());
            HttpUrl componentVersionUriHttpUrl = new HttpUrl(componentVersionUri);
            componentVersionView.getMeta().setHref(componentVersionUriHttpUrl);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(componentVersionUriHttpUrl), Mockito.eq(ComponentVersionView.class))).thenReturn(componentVersionView);

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
        return new ProjectService(mockBlackDuckService(), mockRequestFactory(), mockLogger(), Mockito.mock(ProjectGetService.class));
    }

    public static BlackDuckBucketService mockBucketService() {
        return new BlackDuckBucketService(mockBlackDuckService(), mockRequestFactory(), mockLogger(), new NoThreadExecutorService());
    }

    public static RequestFactory mockRequestFactory() {
        return new RequestFactory();
    }

    public static IntLogger mockLogger() {
        return new PrintStreamIntLogger(System.out, LogLevel.ERROR);
    }

    private static void mockVulnSingleResponse(String uri, BlackDuckService blackDuckService, ProjectVersionVulnerableBomComponentsItemsVulnerabilityWithRemediationSeverityType severity) {
        VulnerabilityView vulnerabilityView = new VulnerabilityView();
        vulnerabilityView.setSeverity(severity);
        vulnerabilityView.setMeta(new ResourceMetadata());
        try {
            HttpUrl httpUrl = new HttpUrl(uri);
            vulnerabilityView.getMeta().setHref(httpUrl);
            Mockito.when(blackDuckService.getResponse(Mockito.eq(httpUrl), Mockito.eq(VulnerabilityView.class))).thenReturn(vulnerabilityView);
        } catch (IntegrationException ignored) {
        }
    }

}

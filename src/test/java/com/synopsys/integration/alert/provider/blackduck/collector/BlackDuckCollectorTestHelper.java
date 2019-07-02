package com.synopsys.integration.alert.provider.blackduck.collector;

import java.util.Optional;

import org.mockito.Mockito;

import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VulnerabilityView;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucketService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;

public class BlackDuckCollectorTestHelper {
    public static BlackDuckProperties mockProperties() {
        final BlackDuckProperties mockProperties = Mockito.mock(BlackDuckProperties.class);
        final BlackDuckHttpClient mockHttpClient = mockHttpClient();
        final BlackDuckServicesFactory mockServicesFactory = mockServicesFactory();

        Mockito.when(mockProperties.createBlackDuckHttpClientAndLogErrors(Mockito.any())).thenReturn(Optional.of(mockHttpClient));
        Mockito.when(mockProperties.createBlackDuckServicesFactory(Mockito.any(), Mockito.any())).thenReturn(mockServicesFactory);
        return mockProperties;
    }

    public static BlackDuckHttpClient mockHttpClient() {
        return Mockito.mock(BlackDuckHttpClient.class);
    }

    public static BlackDuckServicesFactory mockServicesFactory() {
        final BlackDuckServicesFactory mockServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        final BlackDuckService blackDuckService = mockBlackDuckService();

        final BlackDuckBucketService bucketService = mockBucketService();

        Mockito.when(mockServicesFactory.createBlackDuckService()).thenReturn(blackDuckService);
        Mockito.when(mockServicesFactory.createBlackDuckBucketService()).thenReturn(bucketService);

        return mockServicesFactory;
    }

    public static BlackDuckService mockBlackDuckService() {
        final BlackDuckService mockBlackDuckService = Mockito.mock(BlackDuckService.class);
        try {
            ProjectVersionView projectVersionView = new ProjectVersionView();

            final UriSingleResponse mockProjectSingleResponse1 =
                new UriSingleResponse("https://a-hub-server.blackduck.com/api/projects/d9205017-4630-4f0c-8127-170e1db03d6f/versions/ec2a759d-e27d-4445-adb2-3176f8a78d24", ProjectVersionView.class);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(mockProjectSingleResponse1))).thenReturn(projectVersionView);

            final UriSingleResponse mockProjectSingleResponse2 =
                new UriSingleResponse("https://a-hub-server.blackduck.com/api/projects/fa9ca16d-1238-4795-85d4-f47853a9b06c/versions/6c39d4f1-713d-4702-b9c8-e964e6ec932c", ProjectVersionView.class);
            Mockito.when(mockBlackDuckService.getResponse(Mockito.eq(mockProjectSingleResponse2))).thenReturn(projectVersionView);

            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_1, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_2, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_3, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_4, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_5, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_6, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_7, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_8, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_9, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_10, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_11, mockBlackDuckService, "HIGH");
            mockVulnSingleResponse(BlackDuckVulnerabilityCollectorTest.VULNERABILITY_URL_CVE_12, mockBlackDuckService, "HIGH");

            Mockito.when(mockBlackDuckService.getResponse(Mockito.anyString(), Mockito.any(ProjectVersionView.class.getClass()))).thenReturn(projectVersionView);
        } catch (IntegrationException ignored) {
        }

        return mockBlackDuckService;
    }

    public static BlackDuckBucketService mockBucketService() {
        return new BlackDuckBucketService(mockBlackDuckService(), mockLogger());
    }

    public static IntLogger mockLogger() {
        return new PrintStreamIntLogger(System.out, LogLevel.ERROR);
    }

    private static void mockVulnSingleResponse(String uri, BlackDuckService blackDuckService, String severity) {
        VulnerabilityView vulnerabilityView = new VulnerabilityView();
        vulnerabilityView.setSeverity(severity);
        final UriSingleResponse mockVulnSingleResponse2 =
            new UriSingleResponse(uri, VulnerabilityView.class);
        try {
            Mockito.when(blackDuckService.getResponse(Mockito.eq(mockVulnSingleResponse2))).thenReturn(vulnerabilityView);
        } catch (IntegrationException ignored) {
        }
    }

}

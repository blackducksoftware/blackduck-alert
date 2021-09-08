package com.synopsys.integration.alert.provider.blackduck.processor.detail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.processor.api.detail.DetailedNotificationContent;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.manual.view.BomEditNotificationView;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BomEditNotificationDetailExtractorTest {
    public static final String BOM_EDIT_JSON_PATH = "json/bomEditNotification.json";

    private final Gson gson = new Gson();

    @Test
    public void extractDetailedContentTest() throws IOException, IntegrationException {
        String projectName = "project-name";
        String projectVersionName = "project-version-name";
        Long blackDuckConfigId = 0L;

        String notificationString = TestResourceUtils.readFileToString(BOM_EDIT_JSON_PATH);
        BomEditNotificationView notificationView = gson.fromJson(notificationString, BomEditNotificationView.class);

        NotificationExtractorBlackDuckServicesFactoryCache cache = createCache(blackDuckConfigId, projectName, projectVersionName);
        BomEditNotificationDetailExtractor extractor = new BomEditNotificationDetailExtractor(cache);

        AlertNotificationModel notification = new AlertNotificationModel(0L, blackDuckConfigId, "BlackDuck", "Config 1", null, null, null, null, false);

        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification, notificationView);
        assertEquals(1, detailedNotificationContents.size());

        DetailedNotificationContent detailedNotificationContent = detailedNotificationContents.get(0);

        Optional<String> detailedProjectName = detailedNotificationContent.getProjectName();
        assertTrue(detailedProjectName.isPresent(), "Expected project name to be present");
        assertEquals(projectName, detailedProjectName.get());

        assertTrue(detailedNotificationContent.getPolicyName().isEmpty(), "Expected no policy name to be present");
        assertEquals(0, detailedNotificationContent.getVulnerabilitySeverities().size());
    }

    @Test
    public void extractDetailedContentErrorTest() throws IOException, IntegrationException {
        Long blackDuckConfigId = 0L;

        String notificationString = TestResourceUtils.readFileToString(BOM_EDIT_JSON_PATH);
        BomEditNotificationView notificationView = gson.fromJson(notificationString, BomEditNotificationView.class);

        NotificationExtractorBlackDuckServicesFactoryCache cache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
        Mockito.doThrow(new AlertConfigurationException("Expected Exception thrown creating BlackDuckServicesFactory")).when(cache).retrieveBlackDuckServicesFactory(Mockito.anyLong());

        BomEditNotificationDetailExtractor extractor = new BomEditNotificationDetailExtractor(cache);

        AlertNotificationModel notification = new AlertNotificationModel(0L, blackDuckConfigId, "BlackDuck", "Config 1", null, null, null, null, false);

        List<DetailedNotificationContent> detailedNotificationContents = extractor.extractDetailedContent(notification, notificationView);

        assertEquals(0, detailedNotificationContents.size());
    }

    private NotificationExtractorBlackDuckServicesFactoryCache createCache(Long blackDuckConfigId, String projectName, String projectVersionName) throws IntegrationException {
        ProjectView projectView = Mockito.mock(ProjectView.class);
        Mockito.when(projectView.getName()).thenReturn(projectName);

        ProjectVersionView projectVersionView = Mockito.mock(ProjectVersionView.class);
        Mockito.when(projectVersionView.getVersionName()).thenReturn(projectVersionName);

        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectVersionView.class))).thenReturn(projectVersionView);
        Mockito.when(blackDuckApiClient.getResponse(Mockito.eq(projectVersionView.metaProjectLink()))).thenReturn(projectView);

        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        NotificationExtractorBlackDuckServicesFactoryCache cache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
        Mockito.when(cache.retrieveBlackDuckServicesFactory(Mockito.eq(blackDuckConfigId))).thenReturn(blackDuckServicesFactory);

        return cache;
    }
}

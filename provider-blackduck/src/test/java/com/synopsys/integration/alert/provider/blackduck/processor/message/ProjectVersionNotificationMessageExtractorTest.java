package com.synopsys.integration.alert.provider.blackduck.processor.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderMessageHolder;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectOperation;
import com.synopsys.integration.alert.processor.api.filter.NotificationContentWrapper;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.blackduck.api.manual.component.LicenseLimitNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.ProjectVersionNotificationContent;
import com.synopsys.integration.blackduck.api.manual.enumeration.OperationType;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class ProjectVersionNotificationMessageExtractorTest {
    private static final String PROJECT_URL = "https://projectUrl";
    private static final String PROJECT = "ProjectName";
    private static final String PROJECT_VERSION_URL = "https://projectVersionUrl";
    private static final String PROJECT_VERSION = "ProjectVersionName";

    private final BlackDuckProviderKey providerKey = new BlackDuckProviderKey();

    @Test
    public void extractTest() throws IntegrationException {
        NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.when(blackDuckHttpClient.getBlackDuckUrl()).thenReturn(new HttpUrl("https://a.blackduck.server.example.com"));
        Mockito.when(blackDuckServicesFactory.getBlackDuckHttpClient()).thenReturn(blackDuckHttpClient);
        Mockito.when(servicesFactoryCache.retrieveBlackDuckServicesFactory(Mockito.anyLong())).thenReturn(blackDuckServicesFactory);

        ProjectVersionNotificationContent projectVersionNotificationContent = createProjectVersionNotificationContent();
        NotificationContentWrapper notificationContentWrapper = createNotificationContentWrapper(projectVersionNotificationContent);

        ProjectVersionNotificationMessageExtractor extractor = new ProjectVersionNotificationMessageExtractor(providerKey, servicesFactoryCache);
        ProviderMessageHolder providerMessageHolder = extractor.extract(notificationContentWrapper, projectVersionNotificationContent);

        assertEquals(1, providerMessageHolder.getProjectMessages().size());
        assertEquals(0, providerMessageHolder.getSimpleMessages().size());
        ProjectMessage projectMessage = providerMessageHolder.getProjectMessages().get(0);

        assertEquals(MessageReason.PROJECT_VERSION_STATUS, projectMessage.getMessageReason());
        assertTrue(projectMessage.getOperation().isPresent());
        assertEquals(ProjectOperation.CREATE, projectMessage.getOperation().get());
        assertTrue(projectMessage.getBomComponents().isEmpty());
        assertEquals(PROJECT, projectMessage.getProject().getValue());
        assertTrue(projectMessage.getProject().getUrl().isPresent());
        assertEquals(PROJECT_URL, projectMessage.getProject().getUrl().get());

        assertTrue(projectMessage.getProjectVersion().isPresent());
        LinkableItem projectVersion = projectMessage.getProjectVersion().get();
        assertEquals(PROJECT_VERSION, projectVersion.getValue());
        assertTrue(projectVersion.getUrl().isPresent());
        assertEquals(PROJECT_VERSION_URL, projectVersion.getUrl().get());
    }

    private NotificationContentWrapper createNotificationContentWrapper(ProjectVersionNotificationContent notificationContentComponent) {
        AlertNotificationModel alertNotificationModel = new AlertNotificationModel(1L, 1L, "provider-test", "providerConfigName-test", "notificationType-test", "{content: \"content is here...\"}", DateUtils.createCurrentDateTimestamp(),
            DateUtils.createCurrentDateTimestamp(), false);
        return new NotificationContentWrapper(alertNotificationModel, notificationContentComponent, LicenseLimitNotificationContent.class);
    }

    private ProjectVersionNotificationContent createProjectVersionNotificationContent() {
        ProjectVersionNotificationContent projectVersionNotificationContent = new ProjectVersionNotificationContent();
        projectVersionNotificationContent.setProject(PROJECT_URL);
        projectVersionNotificationContent.setProjectName(PROJECT);
        projectVersionNotificationContent.setProjectVersion(PROJECT_VERSION_URL);
        projectVersionNotificationContent.setProjectVersionName(PROJECT_VERSION);
        projectVersionNotificationContent.setOperationType(OperationType.CREATE);

        return projectVersionNotificationContent;
    }
}

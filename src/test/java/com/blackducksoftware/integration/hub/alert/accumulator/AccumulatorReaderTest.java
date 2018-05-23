package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.hub.api.component.AffectedProjectVersion;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class AccumulatorReaderTest {

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);

        final String componentName = "notification test";
        final String componentVersionUrl = "sss";

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);

        final AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.projectName = "VulnerableProject";
        affectedProjectVersion.projectVersion = "VulnerableProjectUrl";
        affectedProjectVersion.projectVersionName = "1.2.3";

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.componentName = componentName;
        content.componentVersion = componentVersionUrl;
        content.versionName = "1.0.0";
        content.affectedProjectVersions = Arrays.asList(affectedProjectVersion);

        final NotificationDetailResult detail = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(Arrays.asList(detail));

        Mockito.doReturn(restConnection).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactory(Mockito.any());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationDetailResultsPopulated(Mockito.any(), Mockito.any(), Mockito.any());

        final AccumulatorReader accumulatorReader = new AccumulatorReader(globalProperties);

        final NotificationDetailResults actualNotificationResults = accumulatorReader.read();
        assertNotNull(actualNotificationResults);
    }

    @Test
    public void testReadWithNullCreatedAtDate() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final RestConnection restConnection = Mockito.mock(RestConnection.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);

        final String componentName = "notification test";
        final String componentVersionUrl = "sss";

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);

        final AffectedProjectVersion affectedProjectVersion = new AffectedProjectVersion();
        affectedProjectVersion.projectName = "VulnerableProject";
        affectedProjectVersion.projectVersion = "VulnerableProjectUrl";
        affectedProjectVersion.projectVersionName = "1.2.3";

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.componentName = componentName;
        content.componentVersion = componentVersionUrl;
        content.versionName = "1.0.0";
        content.affectedProjectVersions = Arrays.asList(affectedProjectVersion);

        final NotificationDetailResult detail = NotificationGeneratorUtils.createNotificationDetailList(view, content);
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(Arrays.asList(detail));

        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactory(Mockito.any());
        Mockito.doReturn(restConnection).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationDetailResultsPopulated(Mockito.any(), Mockito.any(), Mockito.any());

        final AccumulatorReader accumulatorReader = new AccumulatorReader(globalProperties);

        final NotificationDetailResults actualNotificationResults = accumulatorReader.read();
        assertNotNull(actualNotificationResults);
    }

}

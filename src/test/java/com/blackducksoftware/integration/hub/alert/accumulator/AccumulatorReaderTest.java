package com.blackducksoftware.integration.hub.alert.accumulator;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;

public class AccumulatorReaderTest {

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        // TODO Fix Test
        // final HubService service = Mockito.mock(HubService.class);
        // final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        // final NotificationService notificationService = Mockito.mock(NotificationService.class);
        // final TestLogger logger = new TestLogger();
        // final RestConnection restConnection = new UnauthenticatedRestConnection(logger, null, 300, ProxyInfo.NO_PROXY_INFO, null);
        //
        // final Collection<CommonNotificationState> notificationContentItems = new ArrayList<>();
        // final Date createdAt = new Date();
        // final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        // projectVersionModel.setProjectLink("New project link");
        // final String componentName = "notification test";
        // final String componentVersionUrl = "sss";
        //
        // final NotificationView view = new NotificationView();
        // view.contentType = "application/json";
        // view.createdAt = createdAt;
        // view.type = NotificationType.VULNERABILITY;
        //
        // final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        // content.componentName = componentName;
        // content.componentVersion = componentVersionUrl;
        // content.versionName = "1.0.0";
        //
        // final CommonNotificationState notificationContentItem = new CommonNotificationState(view, content);
        // notificationContentItems.add(notificationContentItem);
        // final NotificationResults notificationResults = new NotificationResults(notificationContentItems, null);
        //
        // Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactoryAndLogErrors(Mockito.any());
        // Mockito.doReturn(restConnection).when(service).getRestConnection();
        // Mockito.doReturn(service).when(hubServicesFactory).createHubService();
        // Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        // Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any());
        // Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationResults(Mockito.any(), Mockito.any());
        //
        // final AccumulatorReader accumulatorReader = new AccumulatorReader(globalProperties);
        //
        // final NotificationResults actualNotificationResults = accumulatorReader.read();
        // assertNotNull(actualNotificationResults);
    }

}

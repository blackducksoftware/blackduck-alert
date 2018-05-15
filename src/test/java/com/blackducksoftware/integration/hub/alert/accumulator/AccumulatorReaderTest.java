package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.proxy.ProxyInfo;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.rest.UnauthenticatedRestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;
import com.blackducksoftware.integration.test.TestLogger;

public class AccumulatorReaderTest {

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final HubService service = Mockito.mock(HubService.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final TestLogger logger = new TestLogger();
        final RestConnection restConnection = new UnauthenticatedRestConnection(logger, null, 300, ProxyInfo.NO_PROXY_INFO, null);
        final NotificationGeneratorUtils notificationUtils = new NotificationGeneratorUtils();

        final List<CommonNotificationState> notificationContentItems = new ArrayList<>();
        final String componentName = "notification test";
        final String componentVersionUrl = "sss";

        final NotificationView view = notificationUtils.createNotificationView(NotificationType.VULNERABILITY);

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.componentName = componentName;
        content.componentVersion = componentVersionUrl;
        content.versionName = "1.0.0";

        final CommonNotificationState notificationContentItem = notificationUtils.createCommonNotificationState(view, content);
        notificationContentItems.add(notificationContentItem);

        final NotificationResults notificationResults = notificationUtils.createNotificationResults(notificationContentItems);

        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactoryAndLogErrors(Mockito.any());
        Mockito.doReturn(restConnection).when(service).getRestConnection();
        Mockito.doReturn(service).when(hubServicesFactory).createHubService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any());
        Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationResults(Mockito.any(), Mockito.any());

        final AccumulatorReader accumulatorReader = new AccumulatorReader(globalProperties);

        final NotificationResults actualNotificationResults = accumulatorReader.read();
        assertNotNull(actualNotificationResults);
    }

    @Test
    public void testReadWithNullCreatedAtDate() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final HubService service = Mockito.mock(HubService.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final TestLogger logger = new TestLogger();
        final RestConnection restConnection = new UnauthenticatedRestConnection(logger, null, 300, ProxyInfo.NO_PROXY_INFO, null);
        final NotificationGeneratorUtils notificationUtils = new NotificationGeneratorUtils();

        final List<CommonNotificationState> notificationContentItems = new ArrayList<>();
        final String componentName = "notification test";
        final String componentVersionUrl = "sss";

        final NotificationView view = notificationUtils.createNotificationView(NotificationType.VULNERABILITY);

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.componentName = componentName;
        content.componentVersion = componentVersionUrl;
        content.versionName = "1.0.0";

        final CommonNotificationState notificationContentItem = notificationUtils.createCommonNotificationState(view, content);
        notificationContentItems.add(notificationContentItem);

        final NotificationResults notificationResults = notificationUtils.createNotificationResults(notificationContentItems);

        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactoryAndLogErrors(Mockito.any());
        Mockito.doReturn(restConnection).when(service).getRestConnection();
        Mockito.doReturn(service).when(hubServicesFactory).createHubService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService(Mockito.any());
        Mockito.doReturn(notificationResults).when(notificationService).getAllNotificationResults(Mockito.any(), Mockito.any());

        final AccumulatorReader accumulatorReader = new AccumulatorReader(globalProperties);

        final NotificationResults actualNotificationResults = accumulatorReader.read();
        assertNotNull(actualNotificationResults);
    }

}

package com.blackducksoftware.integration.alert.provider.hub.accumulator;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.mock.notification.NotificationGeneratorUtils;
import com.blackducksoftware.integration.hub.api.component.AffectedProjectVersion;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.CommonNotificationView;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResult;
import com.blackducksoftware.integration.hub.notification.NotificationDetailResults;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.rest.BlackduckRestConnection;
import com.blackducksoftware.integration.hub.service.CommonNotificationService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.NotificationService;

public class AccumulatorReaderTest {

    @Test
    public void testRead() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final CommonNotificationService commonNotificationService = Mockito.mock(CommonNotificationService.class);

        final String componentName = "notification test";
        final String componentVersionUrl = "sss";

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);
        final List<NotificationView> viewList = Arrays.asList(view);
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
        final List<CommonNotificationView> commonViewList = Arrays.asList(NotificationGeneratorUtils.createCommonNotificationView(view));
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(Arrays.asList(detail));

        Mockito.doReturn(restConnection).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(commonNotificationService).when(hubServicesFactory).createCommonNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(viewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());
        Mockito.doReturn(commonViewList).when(commonNotificationService).getCommonNotifications(viewList);
        Mockito.doReturn(notificationResults).when(commonNotificationService).getNotificationDetailResults(commonViewList);

        final HubAccumulatorReader hubAccumulatorReader = new HubAccumulatorReader(globalProperties);

        final NotificationDetailResults actualNotificationResults = hubAccumulatorReader.read();
        assertNotNull(actualNotificationResults);
    }

    @Test
    public void testReadWithNullCreatedAtDate() throws Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final BlackduckRestConnection restConnection = Mockito.mock(BlackduckRestConnection.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationService notificationService = Mockito.mock(NotificationService.class);
        final CommonNotificationService commonNotificationService = Mockito.mock(CommonNotificationService.class);

        final String componentName = "notification test";
        final String componentVersionUrl = "sss";

        final NotificationView view = NotificationGeneratorUtils.createNotificationView(NotificationType.VULNERABILITY);
        final List<NotificationView> viewList = Arrays.asList(view);

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
        final List<CommonNotificationView> commonViewList = Arrays.asList(NotificationGeneratorUtils.createCommonNotificationView(view));
        final NotificationDetailResults notificationResults = NotificationGeneratorUtils.createNotificationResults(Arrays.asList(detail));

        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactory(Mockito.any(), Mockito.any());
        Mockito.doReturn(restConnection).when(globalProperties).createRestConnectionAndLogErrors(Mockito.any());
        Mockito.doReturn(notificationService).when(hubServicesFactory).createNotificationService();
        Mockito.doReturn(commonNotificationService).when(hubServicesFactory).createCommonNotificationService(Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(viewList).when(notificationService).getAllNotifications(Mockito.any(), Mockito.any());
        Mockito.doReturn(commonViewList).when(commonNotificationService).getCommonNotifications(viewList);
        Mockito.doReturn(notificationResults).when(commonNotificationService).getNotificationDetailResults(commonViewList);

        final HubAccumulatorReader hubAccumulatorReader = new HubAccumulatorReader(globalProperties);

        final NotificationDetailResults actualNotificationResults = hubAccumulatorReader.read();
        assertNotNull(actualNotificationResults);
    }

}

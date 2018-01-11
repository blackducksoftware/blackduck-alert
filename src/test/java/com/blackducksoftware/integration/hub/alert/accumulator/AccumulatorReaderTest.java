package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;

public class AccumulatorReaderTest {

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final NotificationDataService notificationDataService = Mockito.mock(NotificationDataService.class);

        final SortedSet<NotificationContentItem> notificationContentItems = new TreeSet<>();
        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        projectVersionModel.setProjectLink("New project link");
        final String componentName = "notification test";
        final ComponentVersionView componentVersionView = new ComponentVersionView();
        final String componentVersionUrl = "sss";
        final String componentIssueUrl = "ddd";

        final NotificationContentItem notificationContentItem = new NotificationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentVersionUrl, componentIssueUrl);
        notificationContentItems.add(notificationContentItem);
        final NotificationResults notificationResults = new NotificationResults(notificationContentItems, null);

        Mockito.doReturn(hubServicesFactory).when(globalProperties).createHubServicesFactoryAndLogErrors(Mockito.any());
        Mockito.doReturn(notificationDataService).when(hubServicesFactory).createNotificationDataService();
        Mockito.doReturn(notificationResults).when(notificationDataService).getAllNotifications(Mockito.any(), Mockito.any());

        final AccumulatorReader accumulatorReader = new AccumulatorReader(globalProperties);

        final NotificationResults actualNotificationResults = accumulatorReader.read();
        assertNotNull(actualNotificationResults);
    }

}

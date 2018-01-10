package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersionModel;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;

public class AccumulatorProcessorTestIT {

    @Test
    public void testProcess() throws Exception {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties);

        final Date createdAt = new Date();
        final ProjectVersionModel projectVersionModel = new ProjectVersionModel();
        projectVersionModel.setProjectLink("New project link");
        final String componentName = "notification test";
        final ComponentVersionView componentVersionView = new ComponentVersionView();
        final String componentVersionUrl = "sss";
        final String componentIssueUrl = "ddd";

        final NotificationContentItem notificationContentItem = new NotificationContentItem(createdAt, projectVersionModel, componentName, componentVersionView, componentVersionUrl, componentIssueUrl);
        final SortedSet<NotificationContentItem> notificationSet = new TreeSet<>();
        notificationSet.add(notificationContentItem);

        final NotificationResults notificationData = Mockito.mock(NotificationResults.class);
        Mockito.when(notificationData.getNotificationContentItems()).thenReturn(notificationSet);

        final DBStoreEvent storeEvent = accumulatorProcessor.process(notificationData);

        assertNotNull(storeEvent);

        final AccumulatorProcessor accumulatorProcessorNull = new AccumulatorProcessor(null);

        final DBStoreEvent storeEventNull = accumulatorProcessorNull.process(notificationData);
        assertNull(storeEventNull);
    }
}

package com.blackducksoftware.integration.hub.alert.accumulator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationDataService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.LogLevel;
import com.blackducksoftware.integration.log.PrintStreamIntLogger;

public class AccumulatorProcessorTestIT {

    @Test
    public void testProcess() throws Exception {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        // Do this to confirm the properties are set
        final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactory(new PrintStreamIntLogger(System.out, LogLevel.INFO));
        final NotificationDataService notificationDataService = hubServicesFactory.createNotificationDataService();

        final Calendar calendarStart = new Calendar.Builder().setWeekDate(2018, 1, Calendar.WEDNESDAY).build();
        final Calendar calendarEnd = new Calendar.Builder().setWeekDate(2018, 1, Calendar.THURSDAY).build();
        final NotificationResults notificationData = notificationDataService.getAllNotifications(calendarStart.getTime(), calendarEnd.getTime());

        final AccumulatorProcessor accumulatorProcessor = new AccumulatorProcessor(globalProperties);

        final DBStoreEvent storeEvent = accumulatorProcessor.process(notificationData);

        assertNotNull(storeEvent);
        assertTrue(!storeEvent.getNotificationList().isEmpty());
        assertEquals(storeEvent.getEventId().length(), 36);

        final AccumulatorProcessor accumulatorProcessorNull = new AccumulatorProcessor(null);

        final DBStoreEvent storeEventNull = accumulatorProcessorNull.process(notificationData);
        assertNull(storeEventNull);
    }
}

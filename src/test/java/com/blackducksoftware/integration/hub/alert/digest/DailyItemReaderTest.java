package com.blackducksoftware.integration.hub.alert.digest;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.NotificationManager;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;

public class DailyItemReaderTest {

    private TestGlobalProperties globalProperties;

    @Before
    public void initTest() {
        globalProperties = new TestGlobalProperties();
    }

    @Test
    public void testGetDateRange() {
        final DailyItemReader dailyItemReader = new DailyItemReader(null, null);

        final DateRange actualDateRange = dailyItemReader.getDateRange();

        final Date actualEndDate = actualDateRange.getEnd();
        final Date actualStartDate = actualDateRange.getStart();

        final Date now = new Date();

        assertTrue(now.before(actualEndDate));
        assertTrue(now.after(actualStartDate));
    }

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final DailyItemReader dailyItemReader = new DailyItemReader(notificationManager, globalProperties);

        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(new NotificationModel(null, null)));

        final List<NotificationModel> notificationList = dailyItemReader.read();

        assertTrue(!notificationList.isEmpty());

        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());

        final List<NotificationModel> hasReadNotificationList = dailyItemReader.read();

        assertNull(hasReadNotificationList);
    }

    @Test
    public void testReadNull() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());

        final DailyItemReader dailyItemReaderNull = new DailyItemReader(notificationManager, globalProperties);

        final List<NotificationModel> nullNotificationList = dailyItemReaderNull.read();

        assertNull(nullNotificationList);
    }

    @Test
    public void testReadException() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(null);

        final DailyItemReader dailyItemReaderException = new DailyItemReader(notificationManager, globalProperties);

        final List<NotificationModel> nullNotificationList = dailyItemReaderException.read();
        assertNull(nullNotificationList);
    }

    @Test
    public void testUnknownVersionPhoneHome() throws Exception {
        globalProperties.setProductVersionOverride(GlobalProperties.PRODUCT_VERSION_UNKNOWN);
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final DailyItemReader dailyItemReader = new DailyItemReader(notificationManager, globalProperties);

        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(new NotificationModel(null, null)));

        final List<NotificationModel> notificationList = dailyItemReader.read();

        assertTrue(!notificationList.isEmpty());

        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());

        final List<NotificationModel> hasReadNotificationList = dailyItemReader.read();

        assertNull(hasReadNotificationList);
    }
}

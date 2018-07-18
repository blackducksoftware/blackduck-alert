package com.blackducksoftware.integration.alert.digest;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.alert.NotificationManager;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;

public class DailyItemReaderTest {

    @Test
    public void testGetDateRange() {
        final DailyItemReader dailyItemReader = new DailyItemReader(null);

        final DateRange actualDateRange = dailyItemReader.getDateRange();

        final Date actualEndDate = actualDateRange.getEnd();
        final Date actualStartDate = actualDateRange.getStart();

        final long endTime = actualEndDate.getTime();
        final long startTime = actualStartDate.getTime();
        final long runTime = endTime - startTime;

        assertEquals(TimeUnit.DAYS.toMillis(1), runTime);
    }

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        final DailyItemReader dailyItemReader = new DailyItemReader(notificationManager);

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

        final DailyItemReader dailyItemReaderNull = new DailyItemReader(notificationManager);

        final List<NotificationModel> nullNotificationList = dailyItemReaderNull.read();

        assertNull(nullNotificationList);
    }

    @Test
    public void testReadException() throws Exception {
        final NotificationManager notificationManager = Mockito.mock(NotificationManager.class);
        Mockito.when(notificationManager.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(null);

        final DailyItemReader dailyItemReaderException = new DailyItemReader(notificationManager);

        final List<NotificationModel> nullNotificationList = dailyItemReaderException.read();
        assertNull(nullNotificationList);
    }
}

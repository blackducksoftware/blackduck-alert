package com.blackducksoftware.integration.hub.alert.digest;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationRepositoryWrapper;

public class DailyItemReaderTest {

    @Test
    public void testGetDateRange() {
        final DailyItemReader dailyItemReader = new DailyItemReader(null);

        final DateRange actualDateRange = dailyItemReader.getDateRange();

        final Date actualEndDate = actualDateRange.getEnd();
        final Date actualStartDate = actualDateRange.getStart();

        final Date now = new Date();

        assertTrue(now.before(actualEndDate));
        assertTrue(now.after(actualStartDate));
    }

    @Test
    public void testRead() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
        final NotificationRepositoryWrapper notificationRepositoryWrapper = Mockito.mock(NotificationRepositoryWrapper.class);
        final DailyItemReader dailyItemReader = new DailyItemReader(notificationRepositoryWrapper);

        Mockito.when(notificationRepositoryWrapper.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList(new NotificationEntity()));

        final List<NotificationEntity> notificationList = dailyItemReader.read();

        assertTrue(!notificationList.isEmpty());

        Mockito.when(notificationRepositoryWrapper.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());

        final List<NotificationEntity> hasReadNotificationList = dailyItemReader.read();

        assertNull(hasReadNotificationList);
    }

    @Test
    public void testReadNull() throws Exception {
        final NotificationRepositoryWrapper notificationRepositoryWrapper = Mockito.mock(NotificationRepositoryWrapper.class);
        Mockito.when(notificationRepositoryWrapper.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(Arrays.asList());

        final DailyItemReader dailyItemReaderNull = new DailyItemReader(notificationRepositoryWrapper);

        final List<NotificationEntity> nullNotificationList = dailyItemReaderNull.read();

        assertNull(nullNotificationList);
    }

    @Test
    public void testReadException() throws Exception {
        final NotificationRepositoryWrapper notificationRepositoryWrapper = Mockito.mock(NotificationRepositoryWrapper.class);
        Mockito.when(notificationRepositoryWrapper.findByCreatedAtBetween(Mockito.any(), Mockito.any())).thenReturn(null);

        final DailyItemReader dailyItemReaderException = new DailyItemReader(notificationRepositoryWrapper);

        final List<NotificationEntity> nullNotificationList = dailyItemReaderException.read();
        assertNull(nullNotificationList);
    }
}

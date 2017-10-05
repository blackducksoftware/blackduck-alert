package com.blackducksoftware.integration.hub.notification.batch.digest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import com.blackducksoftware.integration.hub.notification.datasource.repository.NotificationRepository;

public class DailyItemReader extends DigestItemReader {

    public DailyItemReader(final NotificationRepository notificationRepository) {
        super(notificationRepository);
    }

    @Override
    public DateRange getDateRange() {
        ZonedDateTime currentTime = ZonedDateTime.now();
        currentTime = currentTime.withZoneSameInstant(ZoneOffset.UTC);
        final ZonedDateTime zonedEndDate = currentTime.withHour(23).withMinute(59).withSecond(59).withNano(9999);
        final ZonedDateTime zonedStartDate = currentTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        final Date startDate = Date.from(zonedStartDate.toInstant());
        final Date endDate = Date.from(zonedEndDate.toInstant());
        return new DateRange(startDate, endDate);
    }
}

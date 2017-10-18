package com.blackducksoftware.integration.hub.alert.batch.digest;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import com.blackducksoftware.integration.hub.alert.datasource.repository.NotificationRepository;

public class RealTimeItemReader extends DigestItemReader {

    public RealTimeItemReader(final NotificationRepository notificationRepository) {
        super(notificationRepository);
    }

    @Override
    public DateRange getDateRange() {
        ZonedDateTime currentTime = ZonedDateTime.now();
        currentTime = currentTime.withZoneSameInstant(ZoneOffset.UTC);
        currentTime = currentTime.withSecond(0).withNano(0);
        final ZonedDateTime zonedEndDate = currentTime.minusMinutes(1);
        final ZonedDateTime zonedStartDate = currentTime.minusMinutes(2);
        final Date startDate = Date.from(zonedStartDate.toInstant());
        final Date endDate = Date.from(zonedEndDate.toInstant());
        return new DateRange(startDate, endDate);
    }
}

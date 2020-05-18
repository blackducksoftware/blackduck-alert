package com.synopsys.integration.alert.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.rest.RestConstants;

public class DateUtilsTest {
    @Test
    public void createCurrentDateTimestampOffsetTest() {
        OffsetDateTime alertDateTime = DateUtils.createCurrentDateTimestamp();
        assertEquals(ZoneOffset.UTC, alertDateTime.getOffset());
    }

    @Test
    public void createCurrentDateTimestampZonedDateTimeTest() {
        OffsetDateTime alertDateTime = DateUtils.createCurrentDateTimestamp();
        ZonedDateTime localZonedDateTime = ZonedDateTime.now();

        OffsetDateTime alertLocalDateTime = alertDateTime.withOffsetSameLocal(localZonedDateTime.getOffset());
        assertEquals(alertLocalDateTime.getHour(), localZonedDateTime.getHour());
        assertEquals(alertLocalDateTime.getMinute(), localZonedDateTime.getMinute());
    }

    @Test
    public void convertDateToAndFromStringTest() throws ParseException {
        String dateFormat = RestConstants.JSON_DATE_FORMAT;
        OffsetDateTime alertDateTime = DateUtils.createCurrentDateTimestamp();

        String jsonDateString = DateUtils.formatDate(alertDateTime, dateFormat);
        OffsetDateTime jsonDateTime = DateUtils.parseDate(jsonDateString, dateFormat);

        if (!alertDateTime.getOffset().equals(jsonDateTime.getOffset())) {
            assertTrue("Either hour or minute cannot match", alertDateTime.getHour() != jsonDateTime.getHour() || alertDateTime.getMinute() != alertDateTime.getMinute());
        }

        OffsetDateTime expectedDateTime = OffsetDateTime.ofInstant(alertDateTime.toInstant(), ZoneOffset.UTC);
        assertEquals(expectedDateTime.getHour(), jsonDateTime.getHour());
        assertEquals(expectedDateTime.getMinute(), jsonDateTime.getMinute());
    }

    @Test
    public void checkCurrentTime() throws InterruptedException {
        OffsetDateTime firstTimeStamp = DateUtils.createCurrentDateTimestamp();
        TimeUnit.SECONDS.sleep(1);
        OffsetDateTime currentDateTimestamp = DateUtils.createCurrentDateTimestamp();

        assertTrue(currentDateTimestamp.isAfter(firstTimeStamp));
        assertTrue(firstTimeStamp.isBefore(currentDateTimestamp));
    }

}

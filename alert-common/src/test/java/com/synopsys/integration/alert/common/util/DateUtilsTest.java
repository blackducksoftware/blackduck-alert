package com.synopsys.integration.alert.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.rest.RestConstants;

 class DateUtilsTest {
     @Test
     void createCurrentDateTimestampOffsetTest() {
         OffsetDateTime alertDateTime = DateUtils.createCurrentDateTimestamp();
         assertEquals(ZoneOffset.UTC, alertDateTime.getOffset());
     }

     @Test
     void createCurrentDateTimestampZonedDateTimeTest() {
         OffsetDateTime alertDateTime = DateUtils.createCurrentDateTimestamp();
         ZonedDateTime localZonedDateTime = ZonedDateTime.now();

         ZonedDateTime reZonedLocalTime = localZonedDateTime.withZoneSameInstant(alertDateTime.getOffset());
         assertEquals(alertDateTime.getHour(), reZonedLocalTime.getHour());
         assertEquals(alertDateTime.getMinute(), reZonedLocalTime.getMinute());
     }

     @Test
     void convertDateToAndFromStringTest() throws ParseException {
         String dateFormat = RestConstants.JSON_DATE_FORMAT;
         OffsetDateTime alertDateTime = DateUtils.createCurrentDateTimestamp();

         String jsonDateString = DateUtils.formatDate(alertDateTime, dateFormat);
         OffsetDateTime jsonDateTime = DateUtils.parseDate(jsonDateString, dateFormat);

         if (!alertDateTime.getOffset().equals(jsonDateTime.getOffset())) {
             assertTrue(alertDateTime.getHour() != jsonDateTime.getHour() || alertDateTime.getMinute() != alertDateTime.getMinute(), "Either hour or minute cannot match");
         }

         OffsetDateTime expectedDateTime = OffsetDateTime.ofInstant(alertDateTime.toInstant(), ZoneOffset.UTC);
        assertEquals(expectedDateTime.getHour(), jsonDateTime.getHour());
        assertEquals(expectedDateTime.getMinute(), jsonDateTime.getMinute());
    }

     @Test
     void checkCurrentTime() throws InterruptedException {
         OffsetDateTime firstTimeStamp = DateUtils.createCurrentDateTimestamp();
         TimeUnit.SECONDS.sleep(1);
         OffsetDateTime currentDateTimestamp = DateUtils.createCurrentDateTimestamp();

         assertTrue(currentDateTimestamp.isAfter(firstTimeStamp));
         assertTrue(firstTimeStamp.isBefore(currentDateTimestamp));
     }

     @Test
     void formatDateAsJsonStringTest() {
         OffsetDateTime dateTime = OffsetDateTime.now();
         String formattedDateTimeString = DateUtils.formatDate(dateTime, RestConstants.JSON_DATE_FORMAT);
         String jsonDateTimeString = DateUtils.formatDateAsJsonString(dateTime);
         assertEquals(formattedDateTimeString, jsonDateTimeString);
     }

     @Test
     void parseDateFromJsonString() throws ParseException {
         OffsetDateTime dateTime = OffsetDateTime.now();
         String formattedDateTimeString = DateUtils.formatDate(dateTime, RestConstants.JSON_DATE_FORMAT);

         OffsetDateTime parsedDate = DateUtils.parseDate(formattedDateTimeString, RestConstants.JSON_DATE_FORMAT);
         OffsetDateTime jsonDateTime = DateUtils.parseDateFromJsonString(formattedDateTimeString);
         assertEquals(parsedDate, jsonDateTime);
     }

     @Test
     void formatDurationFromMillisecondsTest() {
         OffsetDateTime start = OffsetDateTime.now();
         OffsetDateTime end = start.plusMinutes(5);
         Duration duration = Duration.between(start, end);
         Long millisecDuration = duration.toMillis();
         String expected = String.format("%sH:%sm:%ss.%s", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
         assertEquals(expected, DateUtils.formatDurationFromMilliseconds(millisecDuration));
     }

     @Test
     void formatDurationFromNanosecondsTest() {
         OffsetDateTime start = OffsetDateTime.now();
         OffsetDateTime end = start.plusMinutes(5);
         Duration duration = Duration.between(start, end);
         Long nanosecDuration = duration.toNanos();
         String expected = String.format(
             "%sH:%sm:%ss.%s",
             duration.toHoursPart(),
             duration.toMinutesPart(),
             duration.toSecondsPart(),
             duration.toNanosPart()
         );
         assertEquals(expected, DateUtils.formatDurationFromNanos(nanosecDuration));
     }

 }

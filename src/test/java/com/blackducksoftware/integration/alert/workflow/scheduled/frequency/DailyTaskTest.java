package com.blackducksoftware.integration.alert.workflow.scheduled.frequency;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Test;

import com.blackducksoftware.integration.alert.common.digest.DateRange;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;

public class DailyTaskTest {

    @Test
    public void testGetTaskName() {
        final DailyTask task = new DailyTask(null, null, null, null);
        assertEquals(DailyTask.class.getName(), task.getTaskName());
    }

    @Test
    public void testDateRange() {
        final DailyTask task = new DailyTask(null, null, null, null);
        final DateRange dateRange = task.getDateRange();
        final ZonedDateTime expectedEndDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final ZonedDateTime expectedStartDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).minusDays(1);

        final ZonedDateTime actualStartDay = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        final ZonedDateTime actualEndDay = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        assertDateIsEqual(expectedStartDay, actualStartDay);
        assertDateIsEqual(expectedEndDay, actualEndDay);
    }

    @Test
    public void testDigestType() {
        final DailyTask task = new DailyTask(null, null, null, null);
        assertEquals(DigestType.DAILY, task.getDigestType());
    }

    private void assertDateIsEqual(final ZonedDateTime expected, final ZonedDateTime actual) {
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());
    }
}

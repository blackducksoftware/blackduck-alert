package com.blackducksoftware.integration.alert.workflow.scheduled.frequency;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.scheduling.TaskScheduler;

import com.blackducksoftware.integration.alert.common.digest.DateRange;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;

public class OnDemandTaskTest {

    @Test
    public void testDateRange() {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final OnDemandTask task = new OnDemandTask(taskScheduler, null, null, null);
        task.scheduleExecutionAtFixedRate(OnDemandTask.DEFAULT_INTERVAL_SECONDS);
        final DateRange dateRange = task.getDateRange();
        final ZonedDateTime expectedEndDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final ZonedDateTime expectedStartDay = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).minusSeconds(OnDemandTask.DEFAULT_INTERVAL_SECONDS);

        final ZonedDateTime actualStartDay = ZonedDateTime.ofInstant(dateRange.getStart().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        final ZonedDateTime actualEndDay = ZonedDateTime.ofInstant(dateRange.getEnd().toInstant(), ZoneId.of(ZoneOffset.UTC.getId()));
        assertDateIsEqual(expectedStartDay, actualStartDay);
        assertDateIsEqual(expectedEndDay, actualEndDay);
        final long difference = ChronoUnit.SECONDS.between(actualStartDay, actualEndDay);
        assertEquals(OnDemandTask.DEFAULT_INTERVAL_SECONDS, difference);
    }

    @Test
    public void testDigestType() {
        final OnDemandTask task = new OnDemandTask(null, null, null, null);
        assertEquals(DigestType.REAL_TIME, task.getDigestType());
    }

    @Test
    public void testGetTaskName() {
        final OnDemandTask task = new OnDemandTask(null, null, null, null);
        assertEquals(OnDemandTask.TASK_NAME, task.getTaskName());
    }

    private void assertDateIsEqual(final ZonedDateTime expected, final ZonedDateTime actual) {
        assertEquals(expected.getYear(), actual.getYear());
        assertEquals(expected.getMonth(), actual.getMonth());
        assertEquals(expected.getDayOfMonth(), actual.getDayOfMonth());

    }

}

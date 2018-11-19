package com.synopsys.integration.alert.channel.email.template;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import com.synopsys.integration.alert.common.model.DateRange;

public class DateRangeTest {

    @Test
    public void testGetTimes() {
        final Date start = new Date(300);
        final Date end = new Date(500);
        final DateRange dateRange = DateRange.of(start, end);

        assertEquals(start, dateRange.getStart());
        assertEquals(end, dateRange.getEnd());
    }
}

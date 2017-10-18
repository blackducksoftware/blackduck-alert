package com.blackducksoftware.integration.hub.alert.batch.digest;

import java.util.Date;

public class DateRange {
    private final Date start;
    private final Date end;

    public DateRange(final Date start, final Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }
}

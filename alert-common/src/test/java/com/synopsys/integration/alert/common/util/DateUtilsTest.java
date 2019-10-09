package com.synopsys.integration.alert.common.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class DateUtilsTest {
    @Test
    public void checkCurrentTime() throws InterruptedException {
        final Date firstTimeStamp = DateUtils.createCurrentDateTimestamp();
        TimeUnit.SECONDS.sleep(1);
        final Date currentDateTimestamp = DateUtils.createCurrentDateTimestamp();

        assertTrue(currentDateTimestamp.after(firstTimeStamp));
        assertTrue(firstTimeStamp.before(currentDateTimestamp));
    }
}

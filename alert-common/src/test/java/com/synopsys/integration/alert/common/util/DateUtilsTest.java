package com.synopsys.integration.alert.common.util;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

public class DateUtilsTest {
    @Test
    public void checkCurrentTime() throws InterruptedException {
        OffsetDateTime firstTimeStamp = DateUtils.createCurrentDateTimestamp();
        TimeUnit.SECONDS.sleep(1);
        OffsetDateTime currentDateTimestamp = DateUtils.createCurrentDateTimestamp();

        assertTrue(currentDateTimestamp.after(firstTimeStamp));
        assertTrue(firstTimeStamp.before(currentDateTimestamp));
    }
}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.message.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.common.util.DateUtils;

public class DateRangeTest {
    @Test
    public void testGetTimes() {
        OffsetDateTime start = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime end = start.plusMinutes(5);
        DateRange dateRange = DateRange.of(start, end);

        assertEquals(start, dateRange.getStart());
        assertEquals(end, dateRange.getEnd());
    }

}

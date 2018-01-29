package com.blackducksoftware.integration.hub.alert.enumeration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StatusEnumTest {

    @Test
    public void testValueOf() {
        final StatusEnum pending = StatusEnum.valueOf("PENDING");
        final StatusEnum success = StatusEnum.valueOf("SUCCESS");
        final StatusEnum failure = StatusEnum.valueOf("FAILURE");

        assertEquals(StatusEnum.PENDING, pending);
        assertEquals(StatusEnum.SUCCESS, success);
        assertEquals(StatusEnum.FAILURE, failure);
    }
}

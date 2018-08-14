package com.synopsys.integration.alert.common.enumeration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DigestTypeEnumTest {

    @Test
    public void testValueOf() {
        final DigestType daily = DigestType.valueOf("DAILY");
        final DigestType realTime = DigestType.valueOf("REAL_TIME");

        assertEquals(DigestType.DAILY, daily);
        assertEquals(DigestType.REAL_TIME, realTime);
    }
}

package com.blackducksoftware.integration.hub.alert.enumeration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DigestTypeEnumTest {

    @Test
    public void testValueOf() {
        final DigestTypeEnum daily = DigestTypeEnum.valueOf("DAILY");
        final DigestTypeEnum realTime = DigestTypeEnum.valueOf("REAL_TIME");

        assertEquals(DigestTypeEnum.DAILY, daily);
        assertEquals(DigestTypeEnum.REAL_TIME, realTime);
    }
}

package com.synopsys.integration.alert.common.enumeration;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class FrequencyTypeEnumTest {

    @Test
    public void testValueOf() {
        final FrequencyType daily = FrequencyType.valueOf("DAILY");
        final FrequencyType realTime = FrequencyType.valueOf("REAL_TIME");

        assertEquals(FrequencyType.DAILY, daily);
        assertEquals(FrequencyType.REAL_TIME, realTime);
    }
}

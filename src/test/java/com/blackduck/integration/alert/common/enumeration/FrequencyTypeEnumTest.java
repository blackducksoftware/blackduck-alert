/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.enumeration;

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

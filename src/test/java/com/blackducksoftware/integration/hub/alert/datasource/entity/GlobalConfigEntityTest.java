/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class GlobalConfigEntityTest {

    @Test
    public void testEmptyModel() {
        final GlobalConfigEntity globalConfigEntity = new GlobalConfigEntity();
        assertEquals(9172607945030111585L, GlobalConfigEntity.getSerialversionuid());

        assertNull(globalConfigEntity.getAccumulatorCron());
        assertNull(globalConfigEntity.getDailyDigestCron());
        assertNull(globalConfigEntity.getHubPassword());
        assertNull(globalConfigEntity.getHubTimeout());
        assertNull(globalConfigEntity.getHubUsername());
        assertNull(globalConfigEntity.getId());

        assertEquals(-1065792035, globalConfigEntity.hashCode());
        final String expectedString = "{\"hubTimeout\":null,\"hubUsername\":null,\"accumulatorCron\":null,\"dailyDigestCron\":null,\"purgeDataCron\":null,\"id\":null}";
        assertEquals(expectedString, globalConfigEntity.toString());

        final GlobalConfigEntity globalConfigEntityNew = new GlobalConfigEntity();
        assertEquals(globalConfigEntity, globalConfigEntityNew);
    }

    @Test
    public void testModel() {
        final Long id = 123L;
        final Integer hubTimeout = 111;
        final String hubUsername = "HubUsername";
        final String hubPassword = "HubPassword";
        final String accumulatorCron = "AccumulatorCron";
        final String dailyDigestCron = "DailyDigestCron";
        final String purgeDataCron = "PurgeDataCron";

        final GlobalConfigEntity globalConfigEntity = new GlobalConfigEntity(hubTimeout, hubUsername, hubPassword, accumulatorCron, dailyDigestCron, purgeDataCron);
        globalConfigEntity.setId(id);

        assertEquals(accumulatorCron, globalConfigEntity.getAccumulatorCron());
        assertEquals(dailyDigestCron, globalConfigEntity.getDailyDigestCron());
        assertEquals(purgeDataCron, globalConfigEntity.getPurgeDataCron());
        assertEquals(hubPassword, globalConfigEntity.getHubPassword());
        assertEquals(hubTimeout, globalConfigEntity.getHubTimeout());
        assertEquals(hubUsername, globalConfigEntity.getHubUsername());
        assertEquals(id, globalConfigEntity.getId());

        assertEquals(-868923204, globalConfigEntity.hashCode());

        final String expectedString = "{\"hubTimeout\":111,\"hubUsername\":\"HubUsername\",\"accumulatorCron\":\"AccumulatorCron\",\"dailyDigestCron\":\"DailyDigestCron\",\"purgeDataCron\":\"PurgeDataCron\",\"id\":123}";
        assertEquals(expectedString, globalConfigEntity.toString());

        final GlobalConfigEntity globalConfigEntityNew = new GlobalConfigEntity(hubTimeout, hubUsername, hubPassword, accumulatorCron, dailyDigestCron, purgeDataCron);
        globalConfigEntityNew.setId(id);

        assertEquals(globalConfigEntity, globalConfigEntityNew);
    }
}

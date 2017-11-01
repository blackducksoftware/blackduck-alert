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
        assertNull(globalConfigEntity.getHubAlwaysTrustCertificate());
        assertNull(globalConfigEntity.getHubPassword());
        assertNull(globalConfigEntity.getHubProxyHost());
        assertNull(globalConfigEntity.getHubProxyPassword());
        assertNull(globalConfigEntity.getHubProxyPort());
        assertNull(globalConfigEntity.getHubProxyUsername());
        assertNull(globalConfigEntity.getHubTimeout());
        assertNull(globalConfigEntity.getHubUrl());
        assertNull(globalConfigEntity.getHubUsername());
        assertNull(globalConfigEntity.getId());

        assertEquals(1110457057, globalConfigEntity.hashCode());

        final String expectedString = "{\"hubUrl\":null,\"hubTimeout\":null,\"hubUsername\":null,\"hubPassword\":null,\"hubProxyHost\":null,\"hubProxyPort\":null,\"hubProxyUsername\":null,\"hubAlwaysTrustCertificate\":null,\"accumulatorCron\":null,\"dailyDigestCron\":null,\"id\":null}";
        assertEquals(expectedString, globalConfigEntity.toString());

        final GlobalConfigEntity globalConfigEntityNew = new GlobalConfigEntity();
        assertEquals(globalConfigEntity, globalConfigEntityNew);
    }

    @Test
    public void testModel() {
        final Long id = 123L;
        final String hubUrl = "HubUrl";
        final Integer hubTimeout = 111;
        final String hubUsername = "HubUsername";
        final String hubPassword = "HubPassword";
        final String hubProxyHost = "HubProxyHost";
        final String hubProxyPort = "HubProxyPort";
        final String hubProxyUsername = "HubProxyUsername";
        final String hubProxyPassword = "HubProxyPassword";
        final Boolean hubAlwaysTrustCertificate = true;
        final String accumulatorCron = "AccumulatorCron";
        final String dailyDigestCron = "DailyDigestCron";

        final GlobalConfigEntity globalConfigEntity = new GlobalConfigEntity(hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate, accumulatorCron,
                dailyDigestCron);
        globalConfigEntity.setId(id);

        assertEquals(accumulatorCron, globalConfigEntity.getAccumulatorCron());
        assertEquals(dailyDigestCron, globalConfigEntity.getDailyDigestCron());
        assertEquals(hubAlwaysTrustCertificate, globalConfigEntity.getHubAlwaysTrustCertificate());
        assertEquals(hubPassword, globalConfigEntity.getHubPassword());
        assertEquals(hubProxyHost, globalConfigEntity.getHubProxyHost());
        assertEquals(hubProxyPassword, globalConfigEntity.getHubProxyPassword());
        assertEquals(hubProxyPort, globalConfigEntity.getHubProxyPort());
        assertEquals(hubProxyUsername, globalConfigEntity.getHubProxyUsername());
        assertEquals(hubTimeout, globalConfigEntity.getHubTimeout());
        assertEquals(hubUrl, globalConfigEntity.getHubUrl());
        assertEquals(hubUsername, globalConfigEntity.getHubUsername());
        assertEquals(id, globalConfigEntity.getId());

        assertEquals(-1100051312, globalConfigEntity.hashCode());

        final String expectedString = "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":111,\"hubUsername\":\"HubUsername\",\"hubPassword\":\"HubPassword\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"HubProxyPort\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubAlwaysTrustCertificate\":true,\"accumulatorCron\":\"AccumulatorCron\",\"dailyDigestCron\":\"DailyDigestCron\",\"id\":123}";
        assertEquals(expectedString, globalConfigEntity.toString());

        final GlobalConfigEntity globalConfigEntityNew = new GlobalConfigEntity(hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate, accumulatorCron,
                dailyDigestCron);
        globalConfigEntityNew.setId(id);

        assertEquals(globalConfigEntity, globalConfigEntityNew);
    }
}

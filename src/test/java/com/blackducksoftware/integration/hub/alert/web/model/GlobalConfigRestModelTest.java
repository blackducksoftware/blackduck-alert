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
package com.blackducksoftware.integration.hub.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class GlobalConfigRestModelTest {

    @Test
    public void testEmptyModel() {
        final GlobalConfigRestModel globalConfigRestModel = new GlobalConfigRestModel();
        assertEquals(9172607945030111585L, GlobalConfigRestModel.getSerialversionuid());

        assertNull(globalConfigRestModel.getAccumulatorCron());
        assertNull(globalConfigRestModel.getDailyDigestCron());
        assertNull(globalConfigRestModel.getHubAlwaysTrustCertificate());
        assertNull(globalConfigRestModel.getHubPassword());
        assertNull(globalConfigRestModel.getHubProxyHost());
        assertNull(globalConfigRestModel.getHubProxyPassword());
        assertNull(globalConfigRestModel.getHubProxyPort());
        assertNull(globalConfigRestModel.getHubProxyUsername());
        assertNull(globalConfigRestModel.getHubTimeout());
        assertNull(globalConfigRestModel.getHubUrl());
        assertNull(globalConfigRestModel.getHubUsername());
        assertNull(globalConfigRestModel.getId());

        assertEquals(1110457057, globalConfigRestModel.hashCode());

        final String expectedString = "{\"hubUrl\":null,\"hubTimeout\":null,\"hubUsername\":null,\"hubPassword\":null,\"hubProxyHost\":null,\"hubProxyPort\":null,\"hubProxyUsername\":null,\"hubAlwaysTrustCertificate\":null,\"accumulatorCron\":null,\"dailyDigestCron\":null,\"id\":null}";
        assertEquals(expectedString, globalConfigRestModel.toString());

        final GlobalConfigRestModel globalConfigRestModelNew = new GlobalConfigRestModel();
        assertEquals(globalConfigRestModel, globalConfigRestModelNew);
    }

    @Test
    public void testModel() {
        final String id = "Id";
        final String hubUrl = "HubUrl";
        final String hubTimeout = "HubTimeout";
        final String hubUsername = "HubUsername";
        final String hubPassword = "HubPassword";
        final String hubProxyHost = "HubProxyHost";
        final String hubProxyPort = "HubProxyPort";
        final String hubProxyUsername = "HubProxyUsername";
        final String hubProxyPassword = "HubProxyPassword";
        final String hubAlwaysTrustCertificate = "HubAlwaysTrustCertificate";
        final String accumulatorCron = "AccumulatorCron";
        final String dailyDigestCron = "DailyDigestCron";

        final GlobalConfigRestModel globalConfigRestModel = new GlobalConfigRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate,
                accumulatorCron, dailyDigestCron);
        assertEquals(accumulatorCron, globalConfigRestModel.getAccumulatorCron());
        assertEquals(dailyDigestCron, globalConfigRestModel.getDailyDigestCron());
        assertEquals(hubAlwaysTrustCertificate, globalConfigRestModel.getHubAlwaysTrustCertificate());
        assertEquals(hubPassword, globalConfigRestModel.getHubPassword());
        assertEquals(hubProxyHost, globalConfigRestModel.getHubProxyHost());
        assertEquals(hubProxyPassword, globalConfigRestModel.getHubProxyPassword());
        assertEquals(hubProxyPort, globalConfigRestModel.getHubProxyPort());
        assertEquals(hubProxyUsername, globalConfigRestModel.getHubProxyUsername());
        assertEquals(hubTimeout, globalConfigRestModel.getHubTimeout());
        assertEquals(hubUrl, globalConfigRestModel.getHubUrl());
        assertEquals(hubUsername, globalConfigRestModel.getHubUsername());
        assertEquals(id, globalConfigRestModel.getId());

        assertEquals(-1538780567, globalConfigRestModel.hashCode());

        final String expectedString = "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":\"HubTimeout\",\"hubUsername\":\"HubUsername\",\"hubPassword\":\"HubPassword\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"HubProxyPort\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubAlwaysTrustCertificate\":\"HubAlwaysTrustCertificate\",\"accumulatorCron\":\"AccumulatorCron\",\"dailyDigestCron\":\"DailyDigestCron\",\"id\":\"Id\"}";
        assertEquals(expectedString, globalConfigRestModel.toString());

        final GlobalConfigRestModel globalConfigRestModelNew = new GlobalConfigRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate,
                accumulatorCron, dailyDigestCron);
        assertEquals(globalConfigRestModel, globalConfigRestModelNew);
    }
}

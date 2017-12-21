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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHubConfigRestModel;

public class GlobalConfigRestModelTest {

    @Test
    public void testEmptyModel() {
        final GlobalHubConfigRestModel globalConfigRestModel = new GlobalHubConfigRestModel();
        assertEquals(9172607945030111585L, GlobalHubConfigRestModel.getSerialversionuid());

        assertNull(globalConfigRestModel.getHubAlwaysTrustCertificate());
        assertNull(globalConfigRestModel.getHubPassword());
        assertFalse(globalConfigRestModel.isHubPasswordIsSet());
        assertNull(globalConfigRestModel.getHubProxyHost());
        assertNull(globalConfigRestModel.getHubProxyPassword());
        assertFalse(globalConfigRestModel.isHubProxyPasswordIsSet());
        assertNull(globalConfigRestModel.getHubProxyPort());
        assertNull(globalConfigRestModel.getHubProxyUsername());
        assertNull(globalConfigRestModel.getHubTimeout());
        assertNull(globalConfigRestModel.getHubUrl());
        assertNull(globalConfigRestModel.getHubUsername());
        assertNull(globalConfigRestModel.getId());

        final int restModelHash = globalConfigRestModel.hashCode();
        assertEquals(-1151643201, restModelHash);

        final String expectedString = "{\"hubUrl\":null,\"hubTimeout\":null,\"hubUsername\":null,\"hubPasswordIsSet\":false,\"hubProxyHost\":null,\"hubProxyPort\":null,\"hubProxyUsername\":null,\"hubProxyPasswordIsSet\":false,\"hubAlwaysTrustCertificate\":null,\"id\":null}";
        assertEquals(expectedString, globalConfigRestModel.toString());

        final GlobalHubConfigRestModel globalConfigRestModelNew = new GlobalHubConfigRestModel();
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

        final GlobalHubConfigRestModel globalConfigRestModel = new GlobalHubConfigRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, true, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, true,
                hubAlwaysTrustCertificate);
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

        final int restModelHash = globalConfigRestModel.hashCode();
        assertEquals(-1414035720, restModelHash);

        final String expectedString = "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":\"HubTimeout\",\"hubUsername\":\"HubUsername\",\"hubPasswordIsSet\":true,\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"HubProxyPort\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubProxyPasswordIsSet\":true,\"hubAlwaysTrustCertificate\":\"HubAlwaysTrustCertificate\",\"id\":\"Id\"}";
        assertEquals(expectedString, globalConfigRestModel.toString());

        final GlobalHubConfigRestModel globalConfigRestModelNew = new GlobalHubConfigRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, true, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, true,
                hubAlwaysTrustCertificate);
        assertEquals(globalConfigRestModel, globalConfigRestModelNew);
    }
}

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
package com.blackducksoftware.integration.hub.alert.web.model.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.GlobalHubMockUtils;

public class GlobalHubConfigRestModelTest extends GlobalRestModelTest<GlobalHubConfigRestModel> {
    private static final GlobalHubMockUtils mockUtils = new GlobalHubMockUtils();

    public GlobalHubConfigRestModelTest() {
        super(mockUtils, GlobalHubConfigRestModel.class);
    }

    @Override
    public void assertGlobalRestModelFieldsNull(final GlobalHubConfigRestModel restModel) {
        assertNull(restModel.getHubAlwaysTrustCertificate());
        assertNull(restModel.getHubPassword());
        assertNull(restModel.getHubProxyHost());
        assertNull(restModel.getHubProxyPassword());
        assertNull(restModel.getHubProxyPort());
        assertNull(restModel.getHubProxyUsername());
        assertNull(restModel.getHubTimeout());
        assertNull(restModel.getHubUrl());
        assertNull(restModel.getHubUsername());
        assertNull(restModel.getId());
    }

    @Override
    public long globalRestModelSerialId() {
        return GlobalHubConfigRestModel.getSerialversionuid();
    }

    @Override
    public int emptyGlobalRestModelHashCode() {
        return -2120005431;
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalHubConfigRestModel restModel) {
        assertEquals(mockUtils.getHubAlwaysTrustCertificate(), restModel.getHubAlwaysTrustCertificate());
        assertEquals(mockUtils.getHubPassword(), restModel.getHubPassword());
        assertEquals(mockUtils.getHubProxyHost(), restModel.getHubProxyHost());
        assertEquals(mockUtils.getHubProxyPassword(), restModel.getHubProxyPassword());
        assertEquals(mockUtils.getHubProxyPort(), restModel.getHubProxyPort());
        assertEquals(mockUtils.getHubProxyUsername(), restModel.getHubProxyUsername());
        assertEquals(mockUtils.getHubTimeout(), restModel.getHubTimeout());
        assertEquals(mockUtils.getHubUrl(), restModel.getHubUrl());
        assertEquals(mockUtils.getHubUsername(), restModel.getHubUsername());
        assertEquals(mockUtils.getId(), restModel.getId());
    }

    @Override
    public int globalRestModelHashCode() {
        return 819265526;
    }

    // @Test
    // public void testEmptyModel() {
    // final GlobalHubConfigRestModel globalConfigRestModel = new GlobalHubConfigRestModel();
    // assertEquals(9172607945030111585L, GlobalHubConfigRestModel.getSerialversionuid());
    //
    // assertNull(globalConfigRestModel.getHubAlwaysTrustCertificate());
    // assertNull(globalConfigRestModel.getHubPassword());
    // assertNull(globalConfigRestModel.getHubProxyHost());
    // assertNull(globalConfigRestModel.getHubProxyPassword());
    // assertNull(globalConfigRestModel.getHubProxyPort());
    // assertNull(globalConfigRestModel.getHubProxyUsername());
    // assertNull(globalConfigRestModel.getHubTimeout());
    // assertNull(globalConfigRestModel.getHubUrl());
    // assertNull(globalConfigRestModel.getHubUsername());
    // assertNull(globalConfigRestModel.getId());
    //
    // assertEquals(-2120005431, globalConfigRestModel.hashCode());
    //
    // final String expectedString = "{\"hubUrl\":null,\"hubTimeout\":null,\"hubUsername\":null,\"hubProxyHost\":null,\"hubProxyPort\":null,\"hubProxyUsername\":null,\"hubAlwaysTrustCertificate\":null,\"id\":null}";
    // assertEquals(expectedString, globalConfigRestModel.toString());
    //
    // final GlobalHubConfigRestModel globalConfigRestModelNew = new GlobalHubConfigRestModel();
    // assertEquals(globalConfigRestModel, globalConfigRestModelNew);
    // }
    //
    // @Test
    // public void testModel() {
    // final String id = "Id";
    // final String hubUrl = "HubUrl";
    // final String hubTimeout = "HubTimeout";
    // final String hubUsername = "HubUsername";
    // final String hubPassword = "HubPassword";
    // final String hubProxyHost = "HubProxyHost";
    // final String hubProxyPort = "HubProxyPort";
    // final String hubProxyUsername = "HubProxyUsername";
    // final String hubProxyPassword = "HubProxyPassword";
    // final String hubAlwaysTrustCertificate = "HubAlwaysTrustCertificate";
    //
    // final GlobalHubConfigRestModel globalConfigRestModel = new GlobalHubConfigRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate);
    // assertEquals(hubAlwaysTrustCertificate, globalConfigRestModel.getHubAlwaysTrustCertificate());
    // assertEquals(hubPassword, globalConfigRestModel.getHubPassword());
    // assertEquals(hubProxyHost, globalConfigRestModel.getHubProxyHost());
    // assertEquals(hubProxyPassword, globalConfigRestModel.getHubProxyPassword());
    // assertEquals(hubProxyPort, globalConfigRestModel.getHubProxyPort());
    // assertEquals(hubProxyUsername, globalConfigRestModel.getHubProxyUsername());
    // assertEquals(hubTimeout, globalConfigRestModel.getHubTimeout());
    // assertEquals(hubUrl, globalConfigRestModel.getHubUrl());
    // assertEquals(hubUsername, globalConfigRestModel.getHubUsername());
    // assertEquals(id, globalConfigRestModel.getId());
    //
    // assertEquals(1740245846, globalConfigRestModel.hashCode());
    //
    // final String expectedString =
    // "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":\"HubTimeout\",\"hubUsername\":\"HubUsername\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"HubProxyPort\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubAlwaysTrustCertificate\":\"HubAlwaysTrustCertificate\",\"id\":\"Id\"}";
    // assertEquals(expectedString, globalConfigRestModel.toString());
    //
    // final GlobalHubConfigRestModel globalConfigRestModelNew = new GlobalHubConfigRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate);
    // assertEquals(globalConfigRestModel, globalConfigRestModelNew);
    // }
}

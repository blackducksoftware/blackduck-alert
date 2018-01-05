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
        assertNull(restModel.getHubApiKey());
        assertNull(restModel.getHubProxyHost());
        assertNull(restModel.getHubProxyPassword());
        assertNull(restModel.getHubProxyPort());
        assertNull(restModel.getHubProxyUsername());
        assertNull(restModel.getHubTimeout());
        assertNull(restModel.getHubUrl());
        assertNull(restModel.getId());
    }

    @Override
    public long globalRestModelSerialId() {
        return GlobalHubConfigRestModel.getSerialversionuid();
    }

    @Override
    public int emptyGlobalRestModelHashCode() {
        return 902077419;
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalHubConfigRestModel restModel) {
        assertEquals(mockUtils.getHubAlwaysTrustCertificate(), restModel.getHubAlwaysTrustCertificate());
        assertEquals(mockUtils.getHubApiKey(), restModel.getHubApiKey());
        assertEquals(mockUtils.getHubProxyHost(), restModel.getHubProxyHost());
        assertEquals(mockUtils.getHubProxyPassword(), restModel.getHubProxyPassword());
        assertEquals(mockUtils.getHubProxyPort(), restModel.getHubProxyPort());
        assertEquals(mockUtils.getHubProxyUsername(), restModel.getHubProxyUsername());
        assertEquals(mockUtils.getHubTimeout(), restModel.getHubTimeout());
        assertEquals(mockUtils.getHubUrl(), restModel.getHubUrl());
        assertEquals(mockUtils.getId(), restModel.getId());
    }

    @Override
    public int globalRestModelHashCode() {
        return -212692039;
    }
}

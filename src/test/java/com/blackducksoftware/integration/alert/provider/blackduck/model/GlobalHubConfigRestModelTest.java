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
package com.blackducksoftware.integration.alert.provider.blackduck.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.blackducksoftware.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckRestModel;
import com.blackducksoftware.integration.alert.web.model.GlobalRestModelTest;
import com.blackducksoftware.integration.alert.web.provider.blackduck.GlobalBlackDuckConfig;

public class GlobalHubConfigRestModelTest extends GlobalRestModelTest<GlobalBlackDuckConfig> {

    @Override
    public void assertGlobalRestModelFieldsNull(final GlobalBlackDuckConfig restModel) {
        assertNull(restModel.getBlackDuckAlwaysTrustCertificate());
        assertNull(restModel.getBlackDuckApiKey());
        assertNull(restModel.getBlackDuckProxyHost());
        assertNull(restModel.getBlackDuckProxyPassword());
        assertNull(restModel.getBlackDuckProxyPort());
        assertNull(restModel.getBlackDuckProxyUsername());
        assertNull(restModel.getBlackDuckTimeout());
        assertNull(restModel.getBlackDuckUrl());
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalBlackDuckConfig restModel) {
        assertEquals(getMockUtil().getBlackDuckAlwaysTrustCertificate(), restModel.getBlackDuckAlwaysTrustCertificate());
        assertEquals(getMockUtil().getBlackDuckProxyHost(), restModel.getBlackDuckProxyHost());
        assertEquals(getMockUtil().getBlackDuckProxyPassword(), restModel.getBlackDuckProxyPassword());
        assertEquals(getMockUtil().getBlackDuckProxyPort(), restModel.getBlackDuckProxyPort());
        assertEquals(getMockUtil().getBlackDuckProxyUsername(), restModel.getBlackDuckProxyUsername());
        assertEquals(getMockUtil().getBlackDuckTimeout(), restModel.getBlackDuckTimeout());
        assertEquals(getMockUtil().getHubUrl(), restModel.getBlackDuckUrl());
    }

    @Override
    public Class<GlobalBlackDuckConfig> getGlobalRestModelClass() {
        return GlobalBlackDuckConfig.class;
    }

    @Override
    public MockGlobalBlackDuckRestModel getMockUtil() {
        return new MockGlobalBlackDuckRestModel();
    }

    @Test
    public void testSetHubProxyPassword() {
        final GlobalBlackDuckConfig model = getMockUtil().createEmptyGlobalRestModel();

        final String expectedPassword = "expected";
        model.setBlackDuckProxyPassword(expectedPassword);

        assertEquals(expectedPassword, model.getBlackDuckProxyPassword());
    }
}

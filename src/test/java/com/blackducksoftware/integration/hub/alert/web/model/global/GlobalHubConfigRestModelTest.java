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

import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalHubRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalRestModelTest;

public class GlobalHubConfigRestModelTest extends GlobalRestModelTest<GlobalHubConfigRestModel> {

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
    }

    @Override
    public long globalRestModelSerialId() {
        return GlobalHubConfigRestModel.getSerialversionuid();
    }

    @Override
    public int emptyGlobalRestModelHashCode() {
        return -1151643201;
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalHubConfigRestModel restModel) {
        assertEquals(getMockUtil().getHubAlwaysTrustCertificate(), restModel.getHubAlwaysTrustCertificate());
        assertEquals(getMockUtil().getHubPassword(), restModel.getHubPassword());
        assertEquals(getMockUtil().getHubProxyHost(), restModel.getHubProxyHost());
        assertEquals(getMockUtil().getHubProxyPassword(), restModel.getHubProxyPassword());
        assertEquals(getMockUtil().getHubProxyPort(), restModel.getHubProxyPort());
        assertEquals(getMockUtil().getHubProxyUsername(), restModel.getHubProxyUsername());
        assertEquals(getMockUtil().getHubTimeout(), restModel.getHubTimeout());
        assertEquals(getMockUtil().getHubUrl(), restModel.getHubUrl());
        assertEquals(getMockUtil().getHubUsername(), restModel.getHubUsername());
    }

    @Override
    public int globalRestModelHashCode() {
        return 1536081842;
    }

    @Override
    public Class<GlobalHubConfigRestModel> getGlobalRestModelClass() {
        return GlobalHubConfigRestModel.class;
    }

    @Override
    public MockGlobalHubRestModel getMockUtil() {
        return new MockGlobalHubRestModel();
    }
}

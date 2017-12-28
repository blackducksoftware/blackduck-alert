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
package com.blackducksoftware.integration.hub.alert.web.model.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.model.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;

public class HipChatConfigRestModelTest extends RestModelTest<HipChatDistributionRestModel> {
    private final MockHipChatRestModel mockUtils = new MockHipChatRestModel();

    @Override
    public void assertRestModelFieldsNull(final HipChatDistributionRestModel restModel) {
        assertNull(restModel.getRoomId());
        assertNull(restModel.getNotify());
        assertNull(restModel.getColor());
    }

    @Override
    public long restModelSerialId() {
        return HipChatDistributionRestModel.getSerialversionuid();
    }

    @Override
    public int emptyRestModelHashCode() {
        return -1862761851;
    }

    @Override
    public void assertRestModelFieldsFull(final HipChatDistributionRestModel restModel) {
        assertEquals(mockUtils.getRoomId(), restModel.getRoomId());
        assertEquals(mockUtils.getNotify(), restModel.getNotify());
        assertEquals(mockUtils.getColor(), restModel.getColor());
    }

    @Override
    public int restModelHashCode() {
        return 1529350275;
    }

    @Override
    public Class<HipChatDistributionRestModel> getRestModelClass() {
        return HipChatDistributionRestModel.class;
    }

    @Override
    public MockRestModelUtil<HipChatDistributionRestModel> getMockUtil() {
        return mockUtils;
    }

}

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

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;

public class HipChatConfigRestModelTest extends RestModelTest<HipChatDistributionRestModel, GlobalHipChatConfigRestModel, HipChatDistributionConfigEntity, GlobalHipChatConfigEntity> {
    private static final HipChatMockUtils mockUtils = new HipChatMockUtils();

    public HipChatConfigRestModelTest() {
        super(mockUtils);
    }

    @Override
    public void assertRestModelFieldsNull(final HipChatDistributionRestModel restModel) {
        assertNull(restModel.getRoomId());
        assertNull(restModel.getNotify());
        assertNull(restModel.getColor());
    }

    @Override
    public long emptyRestModelSerialId() {
        return 3607759169675906880L;
    }

    @Override
    public int emptyRestModelHashCode() {
        return -2120005431;
    }

    @Override
    public void assertRestModelFieldsFull(final HipChatDistributionRestModel restModel) {
        assertEquals(mockUtils.getRoomId(), restModel.getRoomId());
        assertEquals(mockUtils.getNotify(), restModel.getNotify());
        assertEquals(mockUtils.getColor(), restModel.getColor());
    }

    @Override
    public int restModelHashCode() {
        return -1722320535;
    }

}

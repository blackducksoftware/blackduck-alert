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

import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;

public class HipChatConfigRestModelTest {
    HipChatMockUtils mockUtils = new HipChatMockUtils();

    @Test
    public void testEmptyModel() {
        final HipChatDistributionRestModel hipChatConfigRestModel = new HipChatDistributionRestModel();
        assertEquals(-1179576393408142603L, HipChatDistributionRestModel.getSerialversionuid());

        assertNull(hipChatConfigRestModel.getRoomId());
        assertNull(hipChatConfigRestModel.getNotify());
        assertNull(hipChatConfigRestModel.getColor());
        assertNull(hipChatConfigRestModel.getId());

        final int restModelHash = hipChatConfigRestModel.hashCode();
        assertEquals(-2120005431, restModelHash);

        final String expectedString = mockUtils.getEmptyRestModelJson();
        assertEquals(expectedString, hipChatConfigRestModel.toString());

        final HipChatDistributionRestModel hipChatConfigRestModelNew = new HipChatDistributionRestModel();
        assertEquals(hipChatConfigRestModel, hipChatConfigRestModelNew);
    }

    @Test
    public void testModel() {
        final HipChatDistributionRestModel hipChatConfigRestModel = mockUtils.createRestModel();

        assertEquals(mockUtils.getRoomId(), hipChatConfigRestModel.getRoomId());
        assertEquals(mockUtils.getNotify(), hipChatConfigRestModel.getNotify());
        assertEquals(mockUtils.getColor(), hipChatConfigRestModel.getColor());
        assertEquals(mockUtils.getId(), hipChatConfigRestModel.getDistributionConfigId());
        assertEquals(mockUtils.getCommonId(), hipChatConfigRestModel.getId());

        final int restModelHash = hipChatConfigRestModel.hashCode();
        assertEquals(-1722320535, restModelHash);

        final String expectedString = mockUtils.getRestModelJson();
        assertEquals(expectedString, hipChatConfigRestModel.toString());

        final HipChatDistributionRestModel hipChatConfigRestModelNew = mockUtils.createRestModel();
        assertEquals(hipChatConfigRestModel, hipChatConfigRestModelNew);
    }
}

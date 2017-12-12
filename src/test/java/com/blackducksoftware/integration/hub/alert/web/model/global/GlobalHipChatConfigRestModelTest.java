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

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;

public class GlobalHipChatConfigRestModelTest {
    HipChatMockUtils mockUtils = new HipChatMockUtils();

    @Test
    public void testEmptyModel() {
        final GlobalHipChatConfigRestModel hipChatConfigRestModel = new GlobalHipChatConfigRestModel();
        assertEquals(8852683250883814613L, GlobalHipChatConfigRestModel.getSerialversionuid());

        assertNull(hipChatConfigRestModel.getApiKey());
        assertNull(hipChatConfigRestModel.getId());

        final int restModelHash = hipChatConfigRestModel.hashCode();
        assertEquals(23273, restModelHash);

        final String expectedString = mockUtils.getEmptyGlobalRestModelJson();
        assertEquals(expectedString, hipChatConfigRestModel.toString());

        final GlobalHipChatConfigRestModel hipChatConfigRestModelNew = new GlobalHipChatConfigRestModel();
        assertEquals(hipChatConfigRestModel, hipChatConfigRestModelNew);
    }

    @Test
    public void testModel() {
        final GlobalHipChatConfigRestModel hipChatConfigRestModel = mockUtils.createGlobalRestModel();

        assertEquals(mockUtils.getApiKey(), hipChatConfigRestModel.getApiKey());
        assertEquals(mockUtils.getId(), hipChatConfigRestModel.getId());

        final int restModelHash = hipChatConfigRestModel.hashCode();
        assertEquals(-215716397, restModelHash);

        final String expectedString = mockUtils.getGlobalRestModelJson();
        assertEquals(expectedString, hipChatConfigRestModel.toString());

        final GlobalHipChatConfigRestModel hipChatConfigRestModelNew = mockUtils.createGlobalRestModel();
        assertEquals(hipChatConfigRestModel, hipChatConfigRestModelNew);
    }
}

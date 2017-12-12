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
package com.blackducksoftware.integration.hub.alert.datasource.entity.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;

public class GlobalHipChatConfigEntityTest {
    HipChatMockUtils mockUtils = new HipChatMockUtils();

    @Test
    public void testEmptyModel() {
        final GlobalHipChatConfigEntity hipChatConfigEntity = new GlobalHipChatConfigEntity();
        assertEquals(2791949172564090134L, GlobalHipChatConfigEntity.getSerialversionuid());

        assertNull(hipChatConfigEntity.getApiKey());
        assertNull(hipChatConfigEntity.getId());

        final int configHash = hipChatConfigEntity.hashCode();
        assertEquals(23273, configHash);

        final String expectedString = mockUtils.getEmptyGlobalEntityJson();
        assertEquals(expectedString, hipChatConfigEntity.toString());

        final GlobalHipChatConfigEntity hipChatConfigEntityNew = new GlobalHipChatConfigEntity();
        assertEquals(hipChatConfigEntity, hipChatConfigEntityNew);
    }

    @Test
    public void testModel() {
        final GlobalHipChatConfigEntity hipChatConfigEntity = mockUtils.createGlobalEntity();

        assertEquals(mockUtils.getApiKey(), hipChatConfigEntity.getApiKey());
        assertEquals(Long.valueOf(mockUtils.getId()), hipChatConfigEntity.getId());

        final int configHash = hipChatConfigEntity.hashCode();
        assertEquals(-215716445, configHash);

        final String expectedString = mockUtils.getGlobalEntityJson();
        assertEquals(expectedString, hipChatConfigEntity.toString());

        final GlobalHipChatConfigEntity hipChatConfigEntityNew = mockUtils.createGlobalEntity();
        assertEquals(hipChatConfigEntity, hipChatConfigEntityNew);
    }
}

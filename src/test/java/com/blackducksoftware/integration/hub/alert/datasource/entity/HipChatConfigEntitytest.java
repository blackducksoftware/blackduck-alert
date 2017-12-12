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
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;

public class HipChatConfigEntitytest {
    HipChatMockUtils mockUtils = new HipChatMockUtils();

    @Test
    public void testEmptyModel() {
        final HipChatDistributionConfigEntity hipChatConfigEntity = new HipChatDistributionConfigEntity();
        assertEquals(8645967062445661540L, HipChatDistributionConfigEntity.getSerialversionuid());

        assertNull(hipChatConfigEntity.getRoomId());
        assertNull(hipChatConfigEntity.getColor());
        assertNull(hipChatConfigEntity.getNotify());
        assertNull(hipChatConfigEntity.getId());

        final int configHash = hipChatConfigEntity.hashCode();
        assertEquals(31860737, configHash);

        final String expectedString = mockUtils.getEmptyEntityJson();
        assertEquals(expectedString, hipChatConfigEntity.toString());

        final HipChatDistributionConfigEntity hipChatConfigEntityNew = new HipChatDistributionConfigEntity();
        assertEquals(hipChatConfigEntity, hipChatConfigEntityNew);
    }

    @Test
    public void testModel() {
        final HipChatDistributionConfigEntity hipChatConfigEntity = mockUtils.createEntity();

        assertEquals(Integer.valueOf(mockUtils.getRoomId()), hipChatConfigEntity.getRoomId());
        assertEquals(Boolean.valueOf(mockUtils.getNotify()), hipChatConfigEntity.getNotify());
        assertEquals(mockUtils.getColor(), hipChatConfigEntity.getColor());
        assertEquals(Long.valueOf(mockUtils.getId()), hipChatConfigEntity.getId());

        final int configHash = hipChatConfigEntity.hashCode();
        assertEquals(-789557399, configHash);

        final String expectedString = mockUtils.getEntityJson();
        assertEquals(expectedString, hipChatConfigEntity.toString());

        final HipChatDistributionConfigEntity hipChatConfigEntityNew = mockUtils.createEntity();
        assertEquals(hipChatConfigEntity, hipChatConfigEntityNew);
    }
}

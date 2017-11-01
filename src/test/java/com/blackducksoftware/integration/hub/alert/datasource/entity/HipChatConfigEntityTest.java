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

public class HipChatConfigEntityTest {

    @Test
    public void testEmptyModel() {
        final HipChatConfigEntity hipChatConfigEntity = new HipChatConfigEntity();
        assertEquals(2791949172564090134L, HipChatConfigEntity.getSerialversionuid());

        assertNull(hipChatConfigEntity.getApiKey());
        assertNull(hipChatConfigEntity.getColor());
        assertNull(hipChatConfigEntity.getId());
        assertNull(hipChatConfigEntity.getNotify());
        assertNull(hipChatConfigEntity.getRoomId());

        assertEquals(1178847269, hipChatConfigEntity.hashCode());

        final String expectedString = "{\"roomId\":null,\"notify\":null,\"color\":null,\"id\":null}";
        assertEquals(expectedString, hipChatConfigEntity.toString());

        final HipChatConfigEntity hipChatConfigEntityNew = new HipChatConfigEntity();
        assertEquals(hipChatConfigEntity, hipChatConfigEntityNew);
    }

    @Test
    public void testModel() {
        final Long id = 435L;
        final String apiKey = "ApiKey";
        final Integer roomId = 3245;
        final Boolean notify = true;
        final String color = "Color";

        final HipChatConfigEntity hipChatConfigEntity = new HipChatConfigEntity(apiKey, roomId, notify, color);
        hipChatConfigEntity.setId(id);

        assertEquals(apiKey, hipChatConfigEntity.getApiKey());
        assertEquals(color, hipChatConfigEntity.getColor());
        assertEquals(id, hipChatConfigEntity.getId());
        assertEquals(notify, hipChatConfigEntity.getNotify());
        assertEquals(roomId, hipChatConfigEntity.getRoomId());

        assertEquals(-2001518964, hipChatConfigEntity.hashCode());

        final String expectedString = "{\"roomId\":3245,\"notify\":true,\"color\":\"Color\",\"id\":435}";
        assertEquals(expectedString, hipChatConfigEntity.toString());

        final HipChatConfigEntity hipChatConfigEntityNew = new HipChatConfigEntity(apiKey, roomId, notify, color);
        hipChatConfigEntityNew.setId(id);
        assertEquals(hipChatConfigEntity, hipChatConfigEntityNew);
    }
}

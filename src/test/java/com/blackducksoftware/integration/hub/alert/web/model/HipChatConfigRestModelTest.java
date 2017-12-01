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

public class HipChatConfigRestModelTest {

    @Test
    public void testEmptyModel() {
        final GlobalHipChatConfigRestModel hipChatConfigRestModel = new GlobalHipChatConfigRestModel();
        assertEquals(8852683250883814613L, GlobalHipChatConfigRestModel.getSerialversionuid());

        assertNull(hipChatConfigRestModel.getApiKey());
        assertNull(hipChatConfigRestModel.getColor());
        assertNull(hipChatConfigRestModel.getId());
        assertNull(hipChatConfigRestModel.getNotify());
        assertNull(hipChatConfigRestModel.getRoomId());

        assertEquals(1178847269, hipChatConfigRestModel.hashCode());

        final String expectedString = "{\"roomId\":null,\"notify\":null,\"color\":null,\"id\":null}";
        assertEquals(expectedString, hipChatConfigRestModel.toString());

        final GlobalHipChatConfigRestModel hipChatConfigRestModelNew = new GlobalHipChatConfigRestModel();
        assertEquals(hipChatConfigRestModel, hipChatConfigRestModelNew);
    }

    @Test
    public void testModel() {
        final String id = "Id";
        final String apiKey = "ApiKey";
        final String roomId = "RoomId";
        final String notify = "Notify";
        final String color = "Color";

        final GlobalHipChatConfigRestModel hipChatConfigRestModel = new GlobalHipChatConfigRestModel(id, apiKey, roomId, notify, color);

        assertEquals(apiKey, hipChatConfigRestModel.getApiKey());
        assertEquals(color, hipChatConfigRestModel.getColor());
        assertEquals(id, hipChatConfigRestModel.getId());
        assertEquals(notify, hipChatConfigRestModel.getNotify());
        assertEquals(roomId, hipChatConfigRestModel.getRoomId());

        assertEquals(-901313501, hipChatConfigRestModel.hashCode());

        final String expectedString = "{\"roomId\":\"RoomId\",\"notify\":\"Notify\",\"color\":\"Color\",\"id\":\"Id\"}";
        assertEquals(expectedString, hipChatConfigRestModel.toString());

        final GlobalHipChatConfigRestModel hipChatConfigRestModelNew = new GlobalHipChatConfigRestModel(id, apiKey, roomId, notify, color);
        assertEquals(hipChatConfigRestModel, hipChatConfigRestModelNew);
    }
}

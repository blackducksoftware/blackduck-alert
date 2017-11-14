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

public class HubUsersConfigRestModelTest {
    @Test
    public void testEmptyModel() {
        final HubUsersConfigRestModel model = new HubUsersConfigRestModel();
        assertEquals(-6931277871895144124L, HubUsersConfigRestModel.getSerialversionuid());

        assertNull(model.getId());
        assertNull(model.getUsername());

        final String expectedString = "{\"username\":null,\"id\":null}";
        assertEquals(expectedString, model.toString());

        final HubUsersConfigRestModel newModel = new HubUsersConfigRestModel();
        assertEquals(model, newModel);
    }

    @Test
    public void testModel() {
        final String id = "ID";
        final String username = "user";

        final HubUsersConfigRestModel model = new HubUsersConfigRestModel(id, username);

        assertEquals(id, model.getId());
        assertEquals(username, model.getUsername());
        assertEquals(133199963, model.hashCode());

        final String expectedString = "{\"username\":\"" + username + "\",\"id\":\"" + id + "\"}";
        assertEquals(expectedString, model.toString());

        final HubUsersConfigRestModel newModel = new HubUsersConfigRestModel(id, username);
        assertEquals(model, newModel);
    }
}

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

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;

public class GlobalHubConfigEntityTest {

    @Test
    public void testEmptyModel() {
        final GlobalHubConfigEntity globalConfigEntity = new GlobalHubConfigEntity();
        assertEquals(9172607945030111585L, GlobalHubConfigEntity.getSerialversionuid());

        assertNull(globalConfigEntity.getHubPassword());
        assertNull(globalConfigEntity.getHubTimeout());
        assertNull(globalConfigEntity.getHubUsername());
        assertNull(globalConfigEntity.getId());

        assertEquals(31860737, globalConfigEntity.hashCode());
        final String expectedString = "{\"hubTimeout\":null,\"hubUsername\":null,\"id\":null}";
        assertEquals(expectedString, globalConfigEntity.toString());

        final GlobalHubConfigEntity globalConfigEntityNew = new GlobalHubConfigEntity();
        assertEquals(globalConfigEntity, globalConfigEntityNew);
    }

    @Test
    public void testModel() {
        final Long id = 123L;
        final Integer hubTimeout = 111;
        final String hubUsername = "HubUsername";
        final String hubPassword = "HubPassword";

        final GlobalHubConfigEntity globalConfigEntity = new GlobalHubConfigEntity(hubTimeout, hubUsername, hubPassword);
        globalConfigEntity.setId(id);

        assertEquals(hubPassword, globalConfigEntity.getHubPassword());
        assertEquals(hubTimeout, globalConfigEntity.getHubTimeout());
        assertEquals(hubUsername, globalConfigEntity.getHubUsername());
        assertEquals(id, globalConfigEntity.getId());

        assertEquals(-610617422, globalConfigEntity.hashCode());

        final String expectedString = "{\"hubTimeout\":111,\"hubUsername\":\"HubUsername\",\"id\":123}";
        assertEquals(expectedString, globalConfigEntity.toString());

        final GlobalHubConfigEntity globalConfigEntityNew = new GlobalHubConfigEntity(hubTimeout, hubUsername, hubPassword);
        globalConfigEntityNew.setId(id);

        assertEquals(globalConfigEntity, globalConfigEntityNew);
    }
}

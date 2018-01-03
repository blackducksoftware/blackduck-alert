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

public class GlobalHubConfigEntityTest {

    @Test
    public void testEmptyModel() {
        final GlobalHubConfigEntity globalConfigEntity = new GlobalHubConfigEntity();
        assertEquals(9172607945030111585L, GlobalHubConfigEntity.getSerialversionuid());

        assertNull(globalConfigEntity.getHubApiKey());
        assertNull(globalConfigEntity.getHubTimeout());
        assertNull(globalConfigEntity.getId());

        assertEquals(861101, globalConfigEntity.hashCode());
        final String expectedString = "{\"hubTimeout\":null,\"id\":null}";
        assertEquals(expectedString, globalConfigEntity.toString());

        final GlobalHubConfigEntity globalConfigEntityNew = new GlobalHubConfigEntity();
        assertEquals(globalConfigEntity, globalConfigEntityNew);
    }

    @Test
    public void testModel() {
        final Long id = 123L;
        final Integer hubTimeout = 111;
        final String hubApiKey = "HubApiKey";

        final GlobalHubConfigEntity globalConfigEntity = new GlobalHubConfigEntity(hubTimeout, hubApiKey);
        globalConfigEntity.setId(id);

        assertEquals(hubApiKey, globalConfigEntity.getHubApiKey());
        assertEquals(hubTimeout, globalConfigEntity.getHubTimeout());
        assertEquals(id, globalConfigEntity.getId());

        assertEquals(-1088833407, globalConfigEntity.hashCode());

        final String expectedString = "{\"hubTimeout\":" + hubTimeout + ",\"id\":" + id + "}";
        assertEquals(expectedString, globalConfigEntity.toString());

        final GlobalHubConfigEntity globalConfigEntityNew = new GlobalHubConfigEntity(hubTimeout, hubApiKey);
        globalConfigEntityNew.setId(id);

        assertEquals(globalConfigEntity, globalConfigEntityNew);
    }
}

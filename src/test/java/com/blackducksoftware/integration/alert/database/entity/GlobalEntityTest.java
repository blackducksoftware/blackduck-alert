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
package com.blackducksoftware.integration.alert.database.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONException;
import org.junit.Test;

import com.blackducksoftware.integration.alert.mock.MockGlobalEntityUtil;

public abstract class GlobalEntityTest<GE extends DatabaseEntity> {

    public abstract MockGlobalEntityUtil<GE> getMockUtil();

    @Test
    public void testEmptyGlobalEntity() {
        final GE configEntity = getMockUtil().createEmptyGlobalEntity();

        assertGlobalEntityFieldsNull(configEntity);
        assertNull(configEntity.getId());

        final GE configEntityNew = getMockUtil().createEmptyGlobalEntity();
        assertEquals(configEntity, configEntityNew);
    }

    public abstract Class<GE> getGlobalEntityClass();

    public abstract void assertGlobalEntityFieldsNull(GE entity);

    @Test
    public void testGlobalEntity() throws JSONException {
        final GE configEntity = getMockUtil().createGlobalEntity();

        assertGlobalEntityFieldsFull(configEntity);
        assertEquals(Long.valueOf(getMockUtil().getId()), configEntity.getId());

        final GE configEntityNew = getMockUtil().createGlobalEntity();
        assertEquals(configEntity, configEntityNew);
    }

    public abstract void assertGlobalEntityFieldsFull(GE entity);
}

/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.database.DatabaseEntity;
import com.blackduck.integration.alert.mock.entity.MockEntityUtil;

public abstract class EntityTest<E extends DatabaseEntity> implements BaseEntityTest<E> {

    public abstract MockEntityUtil<E> getMockUtil();

    @Override
    @Test
    public void testEmptyEntity() throws JSONException {
        final E configEntity = getMockUtil().createEmptyEntity();
        assertEntityFieldsNull(configEntity);
        assertNull(configEntity.getId());

        final E configEntityNew = getMockUtil().createEmptyEntity();
        assertEquals(configEntity, configEntityNew);
    }

    @Override
    @Test
    public void testEntity() throws JSONException {
        final E configEntity = getMockUtil().createEntity();

        assertEntityFieldsFull(configEntity);
        assertEquals(Long.valueOf(getMockUtil().getId()), configEntity.getId());

        final E configEntityNew = getMockUtil().createEntity();
        assertEquals(configEntity, configEntityNew);
    }

}

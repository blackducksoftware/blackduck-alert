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
package com.synopsys.integration.alert.database.settings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.database.DatabaseEntity;
import com.synopsys.integration.alert.mock.entity.MockEntityUtil;

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

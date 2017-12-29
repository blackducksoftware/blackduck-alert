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

import java.io.ObjectStreamClass;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;

public abstract class EntityTest<E extends DatabaseEntity> {

    public abstract MockEntityUtil<E> getMockUtil();

    @Test
    public void testEmptyEntity() {
        final E configEntity = getMockUtil().createEmptyEntity();
        assertEquals(entitySerialId(), ObjectStreamClass.lookup(getEntityClass()).getSerialVersionUID());

        assertEntityFieldsNull(configEntity);
        assertNull(configEntity.getId());

        final int configHash = configEntity.hashCode();
        assertEquals(emptyEntityHashCode(), configHash);

        final String expectedString = getMockUtil().getEmptyEntityJson();
        assertEquals(expectedString, configEntity.toString());

        final E configEntityNew = getMockUtil().createEmptyEntity();
        assertEquals(configEntity, configEntityNew);
    }

    public abstract Class<E> getEntityClass();

    public abstract void assertEntityFieldsNull(E entity);

    public abstract long entitySerialId();

    public abstract int emptyEntityHashCode();

    @Test
    public void testEntity() {
        final E configEntity = getMockUtil().createEntity();

        assertEntityFieldsFull(configEntity);
        assertEquals(Long.valueOf(getMockUtil().getId()), configEntity.getId());

        final int configHash = configEntity.hashCode();
        assertEquals(entityHashCode(), configHash);

        final String expectedString = getMockUtil().getEntityJson();
        assertEquals(expectedString, configEntity.toString());

        final E configEntityNew = getMockUtil().createEntity();
        assertEquals(configEntity, configEntityNew);
    }

    public abstract void assertEntityFieldsFull(E entity);

    public abstract int entityHashCode();

}

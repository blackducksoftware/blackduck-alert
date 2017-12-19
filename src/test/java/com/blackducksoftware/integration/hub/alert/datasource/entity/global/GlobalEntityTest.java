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

import java.io.ObjectStreamClass;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;

public abstract class GlobalEntityTest<GE extends DatabaseEntity> {
    private final MockUtils<?, ?, ?, GE> mockUtils;
    private final Class<GE> globalEntityClass;

    public GlobalEntityTest(final MockUtils<?, ?, ?, GE> mockUtils, final Class<GE> globalEntityClass) {
        this.mockUtils = mockUtils;
        this.globalEntityClass = globalEntityClass;
    }

    @Test
    public void testEmptyGlobalEntity() {
        final GE configEntity = mockUtils.createEmptyGlobalEntity();
        assertEquals(globalEntitySerialId(), ObjectStreamClass.lookup(globalEntityClass).getSerialVersionUID());

        assertGlobalEntityFieldsNull(configEntity);
        assertNull(configEntity.getId());

        final int configHash = configEntity.hashCode();
        assertEquals(emptyGlobalEntityHashCode(), configHash);

        final String expectedString = mockUtils.getEmptyGlobalEntityJson();
        assertEquals(expectedString, configEntity.toString());

        final GE configEntityNew = mockUtils.createEmptyGlobalEntity();
        assertEquals(configEntity, configEntityNew);
    }

    public abstract void assertGlobalEntityFieldsNull(GE entity);

    public abstract long globalEntitySerialId();

    public abstract int emptyGlobalEntityHashCode();

    @Test
    public void testGlobalEntity() {
        final GE configEntity = mockUtils.createGlobalEntity();

        assertGlobalEntityFieldsFull(configEntity);
        assertEquals(Long.valueOf(mockUtils.getId()), configEntity.getId());

        final int configHash = configEntity.hashCode();
        assertEquals(globalEntityHashCode(), configHash);

        final String expectedString = mockUtils.getGlobalEntityJson();
        assertEquals(expectedString, configEntity.toString());

        final GE configEntityNew = mockUtils.createGlobalEntity();
        assertEquals(configEntity, configEntityNew);
    }

    public abstract void assertGlobalEntityFieldsFull(GE entity);

    public abstract int globalEntityHashCode();
}

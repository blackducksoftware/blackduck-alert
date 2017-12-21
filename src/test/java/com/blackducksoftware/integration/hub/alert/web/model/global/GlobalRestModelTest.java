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
package com.blackducksoftware.integration.hub.alert.web.model.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ObjectStreamClass;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class GlobalRestModelTest<GR extends ConfigRestModel> {
    private final MockUtils<?, GR, ?, ?> mockUtils;
    private final Class<GR> globalRestModelClass;

    public GlobalRestModelTest(final MockUtils<?, GR, ?, ?> mockUtils, final Class<GR> globalRestModelClass) {
        this.mockUtils = mockUtils;
        this.globalRestModelClass = globalRestModelClass;
    }

    @Test
    public void testEmptyGlobalRestModel() {
        final GR configRestModel = mockUtils.createEmptyGlobalRestModel();
        assertEquals(globalRestModelSerialId(), ObjectStreamClass.lookup(globalRestModelClass).getSerialVersionUID());

        assertGlobalRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(emptyGlobalRestModelHashCode(), configHash);

        final String expectedString = mockUtils.getEmptyGlobalRestModelJson();
        assertEquals(expectedString, configRestModel.toString());

        final GR configRestModelNew = mockUtils.createEmptyGlobalRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract void assertGlobalRestModelFieldsNull(GR restModel);

    public abstract long globalRestModelSerialId();

    public abstract int emptyGlobalRestModelHashCode();

    @Test
    public void testGlobalRestModel() {
        final GR configRestModel = mockUtils.createGlobalRestModel();

        assertGlobalRestModelFieldsFull(configRestModel);
        assertEquals(mockUtils.getId(), configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(globalRestModelHashCode(), configHash);

        final String expectedString = mockUtils.getGlobalRestModelJson();
        assertEquals(expectedString, configRestModel.toString());

        final GR configRestModelNew = mockUtils.createGlobalRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract void assertGlobalRestModelFieldsFull(GR restModel);

    public abstract int globalRestModelHashCode();
}

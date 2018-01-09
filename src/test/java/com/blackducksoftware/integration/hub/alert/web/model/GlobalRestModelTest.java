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

import java.io.ObjectStreamClass;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class GlobalRestModelTest<GR extends ConfigRestModel> {

    public abstract MockGlobalRestModelUtil<GR> getMockUtil();

    @Test
    public void testEmptyGlobalRestModel() {
        final GR configRestModel = getMockUtil().createEmptyGlobalRestModel();
        assertEquals(globalRestModelSerialId(), ObjectStreamClass.lookup(getGlobalRestModelClass()).getSerialVersionUID());

        assertGlobalRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(emptyGlobalRestModelHashCode(), configHash);

        final String expectedString = getMockUtil().getEmptyGlobalRestModelJson();
        assertEquals(expectedString, configRestModel.toString());

        final GR configRestModelNew = getMockUtil().createEmptyGlobalRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract Class<GR> getGlobalRestModelClass();

    public abstract void assertGlobalRestModelFieldsNull(GR restModel);

    public abstract long globalRestModelSerialId();

    public abstract int emptyGlobalRestModelHashCode();

    @Test
    public void testGlobalRestModel() {
        final GR configRestModel = getMockUtil().createGlobalRestModel();

        assertGlobalRestModelFieldsFull(configRestModel);
        assertEquals(String.valueOf(getMockUtil().getId()), configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(globalRestModelHashCode(), configHash);

        final String expectedString = getMockUtil().getGlobalRestModelJson();
        assertEquals(expectedString, configRestModel.toString());

        final GR configRestModelNew = getMockUtil().createGlobalRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract void assertGlobalRestModelFieldsFull(GR restModel);

    public abstract int globalRestModelHashCode();
}

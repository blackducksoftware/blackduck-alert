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

import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalRestModelUtil;
import com.google.gson.Gson;

public abstract class GlobalRestModelTest<GR extends ConfigRestModel> {
    private final Gson gson = new Gson();

    public abstract MockGlobalRestModelUtil<GR> getMockUtil();

    @Test
    public void testEmptyGlobalRestModel() {
        final GR configRestModel = getMockUtil().createEmptyGlobalRestModel();

        assertGlobalRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final String expectedString = getMockUtil().getEmptyGlobalRestModelJson();
        assertEquals(expectedString, gson.toJson(configRestModel));

        final GR configRestModelNew = getMockUtil().createEmptyGlobalRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract Class<GR> getGlobalRestModelClass();

    public abstract void assertGlobalRestModelFieldsNull(GR restModel);

    @Test
    public void testGlobalRestModel() {
        final GR configRestModel = getMockUtil().createGlobalRestModel();

        assertGlobalRestModelFieldsFull(configRestModel);
        assertEquals(String.valueOf(getMockUtil().getId()), configRestModel.getId());

        final String expectedString = getMockUtil().getGlobalRestModelJson();
        assertEquals(expectedString, gson.toJson(configRestModel));

        final GR configRestModelNew = getMockUtil().createGlobalRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract void assertGlobalRestModelFieldsFull(GR restModel);
}

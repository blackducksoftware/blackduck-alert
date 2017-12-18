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

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public abstract class RestModelTest<R extends CommonDistributionConfigRestModel, GR extends ConfigRestModel, E extends DatabaseEntity, GE extends DatabaseEntity> {
    private final MockUtils<R, GR, E, GE> mockUtils;
    private final Class<R> restModelClass;

    public RestModelTest(final MockUtils<R, GR, E, GE> mockUtils, final Class<R> restModelClass) {
        this.mockUtils = mockUtils;
        this.restModelClass = restModelClass;
    }

    @Test
    public void testEmptyRestModel() throws JSONException {
        final R configRestModel = mockUtils.createEmptyRestModel();
        assertEquals(emptyRestModelSerialId(), ObjectStreamClass.lookup(restModelClass).getSerialVersionUID());

        assertRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(emptyRestModelHashCode(), configHash);

        final String expectedString = mockUtils.getEmptyRestModelJson();
        JSONAssert.assertEquals(expectedString, configRestModel.toString(), false);

        final R configRestModelNew = mockUtils.createEmptyRestModel();
        JSONAssert.assertEquals(configRestModel.toString(), configRestModelNew.toString(), false);
    }

    public abstract void assertRestModelFieldsNull(R restModel);

    public abstract long emptyRestModelSerialId();

    public abstract int emptyRestModelHashCode();

    @Test
    public void testRestModel() throws JSONException {
        final R configRestModel = mockUtils.createRestModel();

        assertRestModelFieldsFull(configRestModel);
        assertEquals(mockUtils.getId(), configRestModel.getDistributionConfigId());

        final int configHash = configRestModel.hashCode();
        assertEquals(restModelHashCode(), configHash);

        final String expectedString = mockUtils.getRestModelJson();
        JSONAssert.assertEquals(expectedString, configRestModel.toString(), false);

        final R configRestModelNew = mockUtils.createRestModel();
        JSONAssert.assertEquals(configRestModel.toString(), configRestModelNew.toString(), false);
    }

    public abstract void assertRestModelFieldsFull(R restModel);

    public abstract int restModelHashCode();
}

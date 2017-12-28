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
package com.blackducksoftware.integration.hub.alert.web.model.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ObjectStreamClass;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;

public abstract class RestModelTest<R extends CommonDistributionConfigRestModel> {

    public abstract MockRestModelUtil<R> getMockUtil();

    @Test
    public void testEmptyRestModel() throws JSONException {
        final R configRestModel = getMockUtil().createEmptyRestModel();
        assertEquals(restModelSerialId(), ObjectStreamClass.lookup(getRestModelClass()).getSerialVersionUID());

        assertRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(emptyRestModelHashCode(), configHash);

        final String expectedString = getMockUtil().getEmptyRestModelJson();
        JSONAssert.assertEquals(expectedString, configRestModel.toString(), false);

        final R configRestModelNew = getMockUtil().createEmptyRestModel();
        JSONAssert.assertEquals(configRestModel.toString(), configRestModelNew.toString(), false);
    }

    public abstract Class<R> getRestModelClass();

    public abstract void assertRestModelFieldsNull(R restModel);

    public abstract long restModelSerialId();

    public abstract int emptyRestModelHashCode();

    @Test
    public void testRestModel() throws JSONException {
        final R configRestModel = getMockUtil().createRestModel();

        assertRestModelFieldsFull(configRestModel);
        assertEquals(String.valueOf(getMockUtil().getId()), configRestModel.getDistributionConfigId());

        final int configHash = configRestModel.hashCode();
        assertEquals(restModelHashCode(), configHash);

        final String expectedString = getMockUtil().getRestModelJson();
        JSONAssert.assertEquals(expectedString, configRestModel.toString(), false);

        final R configRestModelNew = getMockUtil().createRestModel();
        JSONAssert.assertEquals(configRestModel.toString(), configRestModelNew.toString(), false);
    }

    public abstract void assertRestModelFieldsFull(R restModel);

    public abstract int restModelHashCode();
}

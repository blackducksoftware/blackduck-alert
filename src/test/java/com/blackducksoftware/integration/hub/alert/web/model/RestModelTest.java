package com.blackducksoftware.integration.hub.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;

public abstract class RestModelTest<R extends ConfigRestModel> {

    public abstract MockRestModelUtil<R> getMockUtil();

    @Test
    public void testEmptyRestModel() throws JSONException {
        final R configRestModel = getMockUtil().createEmptyRestModel();

        assertRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final String expectedString = getMockUtil().getEmptyRestModelJson();
        JSONAssert.assertEquals(expectedString, configRestModel.toString(), false);

        final R configRestModelNew = getMockUtil().createEmptyRestModel();
        JSONAssert.assertEquals(configRestModel.toString(), configRestModelNew.toString(), false);
    }

    public abstract Class<R> getRestModelClass();

    public abstract void assertRestModelFieldsNull(R restModel);

    @Test
    public void testRestModel() throws JSONException {
        final R restModel = getMockUtil().createRestModel();

        assertRestModelFieldsFull(restModel);
        testId(restModel);

        final String expectedString = getMockUtil().getRestModelJson();
        JSONAssert.assertEquals(expectedString, restModel.toString(), false);

        final R configRestModelNew = getMockUtil().createRestModel();
        JSONAssert.assertEquals(restModel.toString(), configRestModelNew.toString(), false);
    }

    public void testId(final R restModel) {
        assertEquals(String.valueOf(getMockUtil().getId()), restModel.getId());
    }

    public abstract void assertRestModelFieldsFull(R restModel);
}

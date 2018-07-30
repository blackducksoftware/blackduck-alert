package com.blackducksoftware.integration.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.google.gson.Gson;

public abstract class RestModelTest<R extends Config> {
    private final Gson gson = new Gson();

    public abstract MockRestModelUtil<R> getMockUtil();

    @Test
    public void testEmptyRestModel() throws JSONException {
        final R configRestModel = getMockUtil().createEmptyRestModel();

        assertRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final String expectedString = getMockUtil().getEmptyRestModelJson();
        JSONAssert.assertEquals(expectedString, gson.toJson(configRestModel), false);

        final R configRestModelNew = getMockUtil().createEmptyRestModel();
        JSONAssert.assertEquals(gson.toJson(configRestModel), configRestModelNew.toString(), false);
    }

    public abstract Class<R> getRestModelClass();

    public abstract void assertRestModelFieldsNull(R restModel);

    @Test
    public void testRestModel() throws JSONException {
        final R restModel = getMockUtil().createRestModel();

        assertRestModelFieldsFull(restModel);
        testId(restModel);

        final String expectedString = getMockUtil().getRestModelJson();
        JSONAssert.assertEquals(expectedString, gson.toJson(restModel), false);

        final R configRestModelNew = getMockUtil().createRestModel();
        JSONAssert.assertEquals(gson.toJson(restModel), gson.toJson(configRestModelNew), false);
    }

    public void testId(final R restModel) {
        assertEquals(String.valueOf(getMockUtil().getId()), restModel.getId());
    }

    public abstract void assertRestModelFieldsFull(R restModel);
}

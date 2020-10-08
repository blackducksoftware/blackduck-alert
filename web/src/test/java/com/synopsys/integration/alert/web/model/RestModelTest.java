package com.synopsys.integration.alert.web.model;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.rest.model.Config;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;

public abstract class RestModelTest<R extends Config> {
    private final Gson gson = new Gson();

    public abstract MockRestModelUtil<R> getMockUtil();

    @Test
    public void testRestModel() throws JSONException {
        R restModel = getMockUtil().createRestModel();

        assertRestModelFieldsFull(restModel);
        testId(restModel);

        String expectedString = getMockUtil().getRestModelJson();
        JSONAssert.assertEquals(expectedString, gson.toJson(restModel), false);

        R configRestModelNew = getMockUtil().createRestModel();
        JSONAssert.assertEquals(gson.toJson(restModel), gson.toJson(configRestModelNew), false);
    }

    public void testId(R restModel) {
        Assert.assertEquals(String.valueOf(getMockUtil().getId()), restModel.getId());
    }

    public abstract void assertRestModelFieldsFull(R restModel);
}

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
package com.blackducksoftware.integration.alert.mock.model.global;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.alert.mock.MockUtils;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.google.gson.Gson;

public abstract class MockGlobalRestModelUtil<GR extends ConfigRestModel> implements MockUtils {
    private final Gson gson = new Gson();

    public abstract GR createGlobalRestModel();

    public abstract GR createEmptyGlobalRestModel();

    public abstract String getGlobalRestModelJson();

    public String getEmptyGlobalRestModelJson() {
        return "{}";
    }

    public void verifyEmptyGlobalRestModel() throws JSONException {
        final String emptyGlobalRestModel = createEmptyGlobalRestModel().toString();
        final String json = getEmptyGlobalRestModelJson();
        JSONAssert.assertEquals(emptyGlobalRestModel, json, false);
    }

    public void verifyGlobalRestModel() throws JSONException {
        final String globalRestModel = gson.toJson(createGlobalRestModel());
        final String json = getGlobalRestModelJson();
        JSONAssert.assertEquals(globalRestModel, json, false);
    }

    @Test
    @Override
    public void testConfiguration() throws JSONException {
        verifyGlobalRestModel();
        verifyEmptyGlobalRestModel();
    }
}

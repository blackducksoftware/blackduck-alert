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
package com.blackduck.integration.alert.mock.model;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackduck.integration.alert.common.rest.model.Config;
import com.blackduck.integration.alert.mock.MockUtils;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.google.gson.Gson;

public abstract class MockRestModelUtil<R extends Config> implements MockUtils {

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    public abstract R createRestModel();

    public abstract String getRestModelJson();

    public void verifyRestModel() throws JSONException {
        final String restModel = gson.toJson(createRestModel());
        final String json = getRestModelJson();
        JSONAssert.assertEquals(restModel, json, false);
    }

    @Test
    @Override
    public void testConfiguration() throws JSONException {
        verifyRestModel();
    }

}

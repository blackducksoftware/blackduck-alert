/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
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

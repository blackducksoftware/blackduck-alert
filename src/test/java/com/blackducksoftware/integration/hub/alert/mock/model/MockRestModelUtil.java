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
package com.blackducksoftware.integration.hub.alert.mock.model;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public interface MockRestModelUtil<R extends ConfigRestModel> extends MockUtils {

    public R createRestModel();

    public R createEmptyRestModel();

    public String getRestModelJson();

    public String getEmptyRestModelJson();

    public default void verifyEmptyRestModel() throws JSONException {
        final String emptyRestModel = createEmptyRestModel().toString();
        final String json = getEmptyRestModelJson();
        JSONAssert.assertEquals(emptyRestModel, json, false);
    }

    public default void verifyRestModel() throws JSONException {
        final String restModel = createRestModel().toString();
        final String json = getRestModelJson();
        JSONAssert.assertEquals(restModel, json, false);
    }
}

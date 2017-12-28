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
package com.blackducksoftware.integration.hub.alert.mock.model.global;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public interface MockGlobalRestModelUtil<GR extends ConfigRestModel> extends MockUtils {

    public GR createGlobalRestModel();

    public GR createEmptyGlobalRestModel();

    public String getGlobalRestModelJson();

    public String getEmptyGlobalRestModelJson();

    public default void verifyEmptyGlobalRestModel() throws JSONException {
        final String emptyGlobalRestModel = createEmptyGlobalRestModel().toString();
        final String json = getEmptyGlobalRestModelJson();
        JSONAssert.assertEquals(emptyGlobalRestModel, json, false);
    }

    public default void verifyGlobalRestModel() throws JSONException {
        final String globalRestModel = createGlobalRestModel().toString();
        final String json = getGlobalRestModelJson();
        JSONAssert.assertEquals(globalRestModel, json, false);
    }
}

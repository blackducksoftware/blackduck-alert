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
package com.blackducksoftware.integration.hub.alert.mock.entity.global;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;

public interface MockGlobalEntityUtil<GE extends DatabaseEntity> extends MockUtils {

    public GE createGlobalEntity();

    public GE createEmptyGlobalEntity();

    public String getGlobalEntityJson();

    public String getEmptyGlobalEntityJson();

    public default void verifyEmptyGlobalEntity() throws JSONException {
        final String emptyGlobalEntity = createEmptyGlobalEntity().toString();
        final String json = getEmptyGlobalEntityJson();
        JSONAssert.assertEquals(emptyGlobalEntity, json, false);
    }

    public default void verifyGlobalEntity() throws JSONException {
        final String globalEntity = createGlobalEntity().toString();
        final String json = getGlobalEntityJson();
        JSONAssert.assertEquals(globalEntity, json, false);
    }
}

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
package com.blackducksoftware.integration.alert.mock.entity.global;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.mock.MockUtils;

public abstract class MockGlobalEntityUtil<GE extends DatabaseEntity> implements MockUtils {

    public abstract GE createGlobalEntity();

    public abstract GE createEmptyGlobalEntity();

    public abstract String getGlobalEntityJson();

    public String getEmptyGlobalEntityJson() {
        return "{}";
    }

    public void verifyEmptyGlobalEntity() throws JSONException {
        final String emptyGlobalEntity = createEmptyGlobalEntity().toString();
        final String json = getEmptyGlobalEntityJson();
        JSONAssert.assertEquals(emptyGlobalEntity, json, false);
    }

    public void verifyGlobalEntity() throws JSONException {
        final String globalEntity = createGlobalEntity().toString();
        final String json = getGlobalEntityJson();
        JSONAssert.assertEquals(globalEntity, json, false);
    }

    @Test
    @Override
    public void testConfiguration() throws JSONException {
        verifyEmptyGlobalEntity();
        verifyGlobalEntity();
    }
}

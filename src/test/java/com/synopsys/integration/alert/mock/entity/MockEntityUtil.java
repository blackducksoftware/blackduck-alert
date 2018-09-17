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
package com.synopsys.integration.alert.mock.entity;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.mock.MockUtils;
import com.synopsys.integration.alert.web.model.Config;

public abstract class MockEntityUtil<E extends DatabaseEntity> implements MockUtils {

    public abstract Config createConfig();

    public abstract Config createEmptyConfig();

    public abstract E createEntity();

    public abstract E createEmptyEntity();

    public abstract String getEntityJson();

    public String getEmptyEntityJson() {
        return "{}";
    }

    public void verifyEmptyEntity() throws JSONException {
        final String emptyEntity = createEmptyEntity().toString();
        final String emptyJson = getEmptyEntityJson();
        JSONAssert.assertEquals(emptyEntity, emptyJson, false);
    }

    public void verifyEntity() throws JSONException {
        final String entity = createEntity().toString();
        final String json = getEntityJson();
        JSONAssert.assertEquals(entity, json, false);
    }

    // @Test
    @Override
    public void testConfiguration() throws JSONException {
        verifyEntity();
        verifyEmptyEntity();
    }
}

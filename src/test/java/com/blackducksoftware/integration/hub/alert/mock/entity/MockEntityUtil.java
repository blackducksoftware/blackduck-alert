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
package com.blackducksoftware.integration.hub.alert.mock.entity;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;

public interface MockEntityUtil<E extends DatabaseEntity> extends MockUtils {

    public E createEntity();

    public E createEmptyEntity();

    public String getEntityJson();

    public String getEmptyEntityJson();

    public default void verifyEmptyEntity() throws JSONException {
        final String emptyEntity = createEmptyEntity().toString();
        final String emptyJson = getEmptyEntityJson();
        JSONAssert.assertEquals(emptyEntity, emptyJson, false);
    }

    public default void verifyEntity() throws JSONException {
        final String entity = createEntity().toString();
        final String json = getEntityJson();
        JSONAssert.assertEquals(entity, json, false);
    }
}

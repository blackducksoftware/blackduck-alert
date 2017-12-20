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
package com.blackducksoftware.integration.hub.alert.mock;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

//TODO Use the new CommonDatabaseEntity instead of DatabaseEntity
public interface MockUtils<R extends CommonDistributionConfigRestModel, GR extends ConfigRestModel, E extends DatabaseEntity, GE extends DatabaseEntity> {

    public GR createGlobalRestModel();

    public GR createEmptyGlobalRestModel();

    public R createRestModel();

    public R createEmptyRestModel();

    public GE createGlobalEntity();

    public GE createEmptyGlobalEntity();

    public E createEntity();

    public E createEmptyEntity();

    public String getGlobalRestModelJson();

    public String getEmptyGlobalRestModelJson();

    public String getRestModelJson();

    public String getEmptyRestModelJson();

    public String getGlobalEntityJson();

    public String getEmptyGlobalEntityJson();

    public String getEntityJson();

    public String getEmptyEntityJson();

    public String getId();

    public default void verifyEmptyEntity() throws JSONException {
        final String emptyEntity = createEmptyEntity().toString();
        JSONAssert.assertEquals(emptyEntity, getEmptyEntityJson(), false);
    }

    public default void verifyEmptyGlobalEntity() throws JSONException {
        final String emptyGlobalEntity = createEmptyGlobalEntity().toString();
        JSONAssert.assertEquals(emptyGlobalEntity, getEmptyGlobalEntityJson(), false);
    }

    public default void verifyEmptyGlobalRestModel() throws JSONException {
        final String emptyGlobalRestModel = createEmptyGlobalRestModel().toString();
        JSONAssert.assertEquals(emptyGlobalRestModel, getEmptyGlobalRestModelJson(), false);
    }

    public default void verifyEmptyRestModel() throws JSONException {
        final String emptyRestModel = createEmptyRestModel().toString();
        JSONAssert.assertEquals(emptyRestModel, getEmptyRestModelJson(), false);
    }

    public default void verifyEntity() throws JSONException {
        final String entity = createEntity().toString();
        JSONAssert.assertEquals(entity, getEntityJson(), false);
    }

    public default void verifyGlobalEntity() throws JSONException {
        final String globalEntity = createGlobalEntity().toString();
        JSONAssert.assertEquals(globalEntity, getGlobalEntityJson(), false);
    }

    public default void verifyGlobalRestModel() throws JSONException {
        final String globalRestModel = createGlobalRestModel().toString();
        JSONAssert.assertEquals(globalRestModel, getGlobalRestModelJson(), false);
    }

    public default void verifyRestModel() throws JSONException {
        final String restModel = createRestModel().toString();
        JSONAssert.assertEquals(restModel, getRestModelJson(), false);
    }

    public default void verifyAllValues() throws JSONException {
        verifyEmptyEntity();
        verifyEmptyGlobalEntity();
        verifyEmptyGlobalRestModel();
        verifyEmptyRestModel();
        verifyEntity();
        verifyGlobalEntity();
        verifyGlobalRestModel();
        verifyRestModel();
    }

}

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

    public default void verifyValues() throws JSONException {
        final String emptyEntity = createEmptyEntity().toString();
        final String emptyGlobalEntity = createEmptyGlobalEntity().toString();
        final String emptyGlobalRestModel = createEmptyGlobalRestModel().toString();
        final String emptyRestModel = createEmptyRestModel().toString();
        final String entity = createEntity().toString();
        final String globalEntity = createGlobalEntity().toString();
        final String globalRestModel = createGlobalRestModel().toString();
        final String restModel = createRestModel().toString();

        JSONAssert.assertEquals(emptyEntity, getEmptyEntityJson(), false);
        JSONAssert.assertEquals(emptyGlobalEntity, getEmptyGlobalEntityJson(), false);
        JSONAssert.assertEquals(emptyGlobalRestModel, getEmptyGlobalRestModelJson(), false);
        JSONAssert.assertEquals(emptyRestModel, getEmptyRestModelJson(), false);
        JSONAssert.assertEquals(entity, getEntityJson(), false);
        JSONAssert.assertEquals(globalEntity, getGlobalEntityJson(), false);
        JSONAssert.assertEquals(globalRestModel, getGlobalRestModelJson(), false);
        JSONAssert.assertEquals(restModel, getRestModelJson(), false);
    }

}

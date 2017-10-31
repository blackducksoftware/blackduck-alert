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
package com.blackducksoftware.integration.hub.alert.web;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

public class ObjectTransformerTest {
    private final MockUtils mockUtils = new MockUtils();

    @Test
    public void transformGlobalModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final GlobalConfigRestModel restModel = mockUtils.createGlobalConfigRestModel();
        final GlobalConfigEntity configEntity = mockUtils.createGlobalConfigEntity();

        final GlobalConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, GlobalConfigEntity.class);
        final GlobalConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, GlobalConfigRestModel.class);
        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    @Test
    public void transformEmailModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final EmailConfigRestModel restModel = mockUtils.createEmailConfigRestModel();
        final EmailConfigEntity configEntity = mockUtils.createEmailConfigEntity();

        final EmailConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, EmailConfigEntity.class);
        final EmailConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, EmailConfigRestModel.class);

        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

    @Test
    public void transformHipchatModels() throws Exception {
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final HipChatConfigRestModel restModel = mockUtils.createHipChatConfigRestModel();
        final HipChatConfigEntity configEntity = mockUtils.createHipChatConfigEntity();

        final HipChatConfigEntity transformedConfigEntity = objectTransformer.configRestModelToDatabaseEntity(restModel, HipChatConfigEntity.class);
        final HipChatConfigRestModel transformedConfigRestModel = objectTransformer.databaseEntityToConfigRestModel(configEntity, HipChatConfigRestModel.class);
        assertEquals(restModel, transformedConfigRestModel);
        assertEquals(configEntity, transformedConfigEntity);
    }

}

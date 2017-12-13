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
package com.blackducksoftware.integration.hub.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public abstract class RestModelTest<R extends CommonDistributionConfigRestModel, GR extends ConfigRestModel, E extends DatabaseEntity, GE extends DatabaseEntity> {
    private final MockUtils<R, GR, E, GE> mockUtils;

    public RestModelTest(final MockUtils<R, GR, E, GE> mockUtils) {
        this.mockUtils = mockUtils;
    }

    @Test
    public void testEmptyRestModel() {
        final R configRestModel = mockUtils.createEmptyRestModel();
        assertEquals(emptyRestModelSerialId(), SlackDistributionConfigEntity.getSerialversionuid());

        assertRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(emptyRestModelHashCode(), configHash);

        final String expectedString = mockUtils.getEmptyRestModelJson();
        assertEquals(expectedString, configRestModel.toString());

        final R configRestModelNew = mockUtils.createEmptyRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract void assertRestModelFieldsNull(R restModel);

    public abstract long emptyRestModelSerialId();

    public abstract int emptyRestModelHashCode();

    @Test
    public void testRestModel() {
        final R configRestModel = mockUtils.createRestModel();

        assertRestModelFieldsFull(configRestModel);
        assertEquals(mockUtils.getId(), configRestModel.getDistributionConfigId());

        final int configHash = configRestModel.hashCode();
        assertEquals(restModelHashCode(), configHash);

        final String expectedString = mockUtils.getRestModelJson();
        assertEquals(expectedString, configRestModel.toString());

        final R configRestModelNew = mockUtils.createRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract void assertRestModelFieldsFull(R restModel);

    public abstract int restModelHashCode();
}

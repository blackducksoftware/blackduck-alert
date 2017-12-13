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
package com.blackducksoftware.integration.hub.alert.web.model.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public abstract class GlobalRestModelTest<R extends CommonDistributionConfigRestModel, GR extends ConfigRestModel, E extends DatabaseEntity, GE extends DatabaseEntity> {
    private final MockUtils<R, GR, E, GE> mockUtils;

    public GlobalRestModelTest(final MockUtils<R, GR, E, GE> mockUtils) {
        this.mockUtils = mockUtils;
    }

    @Test
    public void testEmptyGlobalRestModel() {
        final GR configRestModel = mockUtils.createEmptyGlobalRestModel();
        assertEquals(SlackDistributionConfigEntity.getSerialversionuid(), emptyGlobalRestModelSerialId());

        assertGlobalRestModelFieldsNull(configRestModel);
        assertNull(configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(emptyGlobalRestModelHashCode(), configHash);

        final String expectedString = mockUtils.getEmptyGlobalRestModelJson();
        assertEquals(expectedString, configRestModel.toString());

        final GR configRestModelNew = mockUtils.createEmptyGlobalRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract void assertGlobalRestModelFieldsNull(GR restModel);

    public abstract long emptyGlobalRestModelSerialId();

    public abstract int emptyGlobalRestModelHashCode();

    @Test
    public void testGlobalRestModel() {
        final GR configRestModel = mockUtils.createGlobalRestModel();

        assertGlobalRestModelFieldsFull(configRestModel);
        assertEquals(mockUtils.getId(), configRestModel.getId());

        final int configHash = configRestModel.hashCode();
        assertEquals(configHash, gloablRestModelHashCode());

        final String expectedString = mockUtils.getGlobalRestModelJson();
        assertEquals(expectedString, configRestModel.toString());

        final GR configRestModelNew = mockUtils.createGlobalRestModel();
        assertEquals(configRestModel, configRestModelNew);
    }

    public abstract void assertGlobalRestModelFieldsFull(GR restModel);

    public abstract int gloablRestModelHashCode();
}

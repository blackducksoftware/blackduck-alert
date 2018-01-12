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
package com.blackducksoftware.integration.hub.alert.scheduling.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.web.model.GlobalRestModelTest;

public class GlobalSchedulingConfigRestModelTest extends GlobalRestModelTest<GlobalSchedulingConfigRestModel> {

    @Override
    public void assertGlobalRestModelFieldsNull(final GlobalSchedulingConfigRestModel restModel) {
        assertNull(restModel.getAccumulatorCron());
        assertNull(restModel.getDailyDigestCron());
        assertNull(restModel.getPurgeDataCron());
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalSchedulingConfigRestModel restModel) {
        assertEquals(getMockUtil().getAccumulatorCron(), restModel.getAccumulatorCron());
        assertEquals(getMockUtil().getDailyDigestCron(), restModel.getDailyDigestCron());
        assertEquals(getMockUtil().getPurgeDataCron(), restModel.getPurgeDataCron());
    }

    @Override
    public Class<GlobalSchedulingConfigRestModel> getGlobalRestModelClass() {
        return GlobalSchedulingConfigRestModel.class;
    }

    @Override
    public MockGlobalSchedulingRestModel getMockUtil() {
        return new MockGlobalSchedulingRestModel();
    }

}

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
package com.blackducksoftware.integration.alert.web.scheduling.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.alert.web.model.GlobalRestModelTest;
import com.blackducksoftware.integration.alert.web.scheduling.GlobalSchedulingConfig;

public class GlobalSchedulingConfigRestModelTest extends GlobalRestModelTest<GlobalSchedulingConfig> {

    @Override
    public void assertGlobalRestModelFieldsNull(final GlobalSchedulingConfig restModel) {
        assertNull(restModel.getAccumulatorNextRun());
        assertNull(restModel.getDailyDigestHourOfDay());
        assertNull(restModel.getDailyDigestNextRun());
        assertNull(restModel.getPurgeDataFrequencyDays());
        assertNull(restModel.getPurgeDataNextRun());
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalSchedulingConfig restModel) {
        assertEquals(getMockUtil().getAccumulatorNextRun(), restModel.getAccumulatorNextRun());
        assertEquals(getMockUtil().getDailyDigestHourOfDay(), restModel.getDailyDigestHourOfDay());
        assertEquals(getMockUtil().getDailyDigestNextRun(), restModel.getDailyDigestNextRun());
        assertEquals(getMockUtil().getPurgeDataFrequencyDays(), restModel.getPurgeDataFrequencyDays());
        assertEquals(getMockUtil().getPurgeDataNextRun(), restModel.getPurgeDataNextRun());
    }

    @Override
    public Class<GlobalSchedulingConfig> getGlobalRestModelClass() {
        return GlobalSchedulingConfig.class;
    }

    @Override
    public MockGlobalSchedulingRestModel getMockUtil() {
        return new MockGlobalSchedulingRestModel();
    }

}

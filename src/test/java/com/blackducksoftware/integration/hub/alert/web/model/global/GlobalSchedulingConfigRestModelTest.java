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

import com.blackducksoftware.integration.hub.alert.mock.GlobalSchedulingMockUtils;

public class GlobalSchedulingConfigRestModelTest extends GlobalRestModelTest<GlobalSchedulingConfigRestModel> {
    private static final GlobalSchedulingMockUtils mockUtils = new GlobalSchedulingMockUtils();

    public GlobalSchedulingConfigRestModelTest() {
        super(mockUtils, GlobalSchedulingConfigRestModel.class);
    }

    @Override
    public void assertGlobalRestModelFieldsNull(final GlobalSchedulingConfigRestModel restModel) {
        assertNull(restModel.getAccumulatorCron());
        assertNull(restModel.getDailyDigestCron());
        assertNull(restModel.getPurgeDataCron());
    }

    @Override
    public long globalRestModelSerialId() {
        return GlobalSchedulingConfigRestModel.getSerialversionuid();
    }

    @Override
    public int emptyGlobalRestModelHashCode() {
        return 31860737;
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalSchedulingConfigRestModel restModel) {
        assertEquals(mockUtils.getAccumulatorCron(), restModel.getAccumulatorCron());
        assertEquals(mockUtils.getDailyDigestCron(), restModel.getDailyDigestCron());
        assertEquals(mockUtils.getPurgeDataCron(), restModel.getPurgeDataCron());
    }

    @Override
    public int globalRestModelHashCode() {
        return -1636279514;
    }

}

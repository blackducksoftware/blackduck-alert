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
package com.blackducksoftware.integration.hub.alert.datasource.entity.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.GlobalSchedulingMockUtils;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;

public class GlobalSchedulingConfigEntityTest extends GlobalEntityTest<GlobalSchedulingConfigEntity> {
    private final GlobalSchedulingMockUtils mockUtils = new GlobalSchedulingMockUtils();

    @Override
    public void assertGlobalEntityFieldsNull(final GlobalSchedulingConfigEntity entity) {
        assertNull(entity.getAccumulatorCron());
        assertNull(entity.getDailyDigestCron());
        assertNull(entity.getPurgeDataCron());
    }

    @Override
    public long globalEntitySerialId() {
        return GlobalSchedulingConfigEntity.getSerialversionuid();
    }

    @Override
    public int emptyGlobalEntityHashCode() {
        return 31860737;
    }

    @Override
    public void assertGlobalEntityFieldsFull(final GlobalSchedulingConfigEntity entity) {
        assertEquals(mockUtils.getAccumulatorCron(), entity.getAccumulatorCron());
        assertEquals(mockUtils.getDailyDigestCron(), entity.getDailyDigestCron());
        assertEquals(mockUtils.getPurgeDataCron(), entity.getPurgeDataCron());
    }

    @Override
    public int globalEntityHashCode() {
        return -1636279562;
    }

    @Override
    public MockUtils<?, ?, ?, GlobalSchedulingConfigEntity> getMockUtil() {
        return mockUtils;
    }

    @Override
    public Class<GlobalSchedulingConfigEntity> getGlobalEntityClass() {
        return GlobalSchedulingConfigEntity.class;
    }

}

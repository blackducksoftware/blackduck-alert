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
package com.synopsys.integration.alert.web.scheduling.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.synopsys.integration.alert.database.entity.GlobalEntityTest;
import com.synopsys.integration.alert.database.scheduling.SchedulingConfigEntity;
import com.synopsys.integration.alert.web.scheduling.mock.MockGlobalSchedulingEntity;

public class GlobalSchedulingConfigEntityTest extends GlobalEntityTest<SchedulingConfigEntity> {

    @Override
    public void assertGlobalEntityFieldsNull(final SchedulingConfigEntity entity) {
        assertNull(entity.getDailyDigestHourOfDay());
        assertNull(entity.getPurgeDataFrequencyDays());
    }

    @Override
    public void assertGlobalEntityFieldsFull(final SchedulingConfigEntity entity) {
        assertEquals(getMockUtil().getDailyDigestHourOfDay(), entity.getDailyDigestHourOfDay());
        assertEquals(getMockUtil().getPurgeDataFrequencyDays(), entity.getPurgeDataFrequencyDays());
    }

    @Override
    public Class<SchedulingConfigEntity> getGlobalEntityClass() {
        return SchedulingConfigEntity.class;
    }

    @Override
    public MockGlobalSchedulingEntity getMockUtil() {
        return new MockGlobalSchedulingEntity();
    }

}

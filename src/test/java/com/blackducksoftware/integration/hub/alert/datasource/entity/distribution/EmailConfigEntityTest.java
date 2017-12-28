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
package com.blackducksoftware.integration.hub.alert.datasource.entity.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.entity.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;

public class EmailConfigEntityTest extends EntityTest<EmailGroupDistributionConfigEntity> {
    private final MockEmailEntity mockUtils = new MockEmailEntity();

    @Override
    public MockEntityUtil<EmailGroupDistributionConfigEntity> getMockUtil() {
        return mockUtils;
    }

    @Override
    public Class<EmailGroupDistributionConfigEntity> getEntityClass() {
        return EmailGroupDistributionConfigEntity.class;
    }

    @Override
    public void assertEntityFieldsNull(final EmailGroupDistributionConfigEntity entity) {
        assertNull(entity.getGroupName());
    }

    @Override
    public long entitySerialId() {
        return EmailGroupDistributionConfigEntity.getSerialversionuid();
    }

    @Override
    public int emptyEntityHashCode() {
        return 23273;
    }

    @Override
    public void assertEntityFieldsFull(final EmailGroupDistributionConfigEntity entity) {
        assertEquals(mockUtils.getGroupName(), entity.getGroupName());
    }

    @Override
    public int entityHashCode() {
        return 957142140;
    }

}

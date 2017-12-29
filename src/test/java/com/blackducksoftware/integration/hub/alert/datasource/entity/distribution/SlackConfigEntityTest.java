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

import com.blackducksoftware.integration.hub.alert.mock.MockUtils;
import com.blackducksoftware.integration.hub.alert.mock.SlackMockUtils;

public class SlackConfigEntityTest extends EntityTest<SlackDistributionConfigEntity> {
    private final SlackMockUtils mockUtils = new SlackMockUtils();

    @Override
    public MockUtils<?, ?, SlackDistributionConfigEntity, ?> getMockUtil() {
        return mockUtils;
    }

    @Override
    public void assertEntityFieldsNull(final SlackDistributionConfigEntity entity) {
        assertNull(entity.getWebhook());
        assertNull(entity.getChannelName());
        assertEquals("Hub-alert", entity.getChannelUsername());
    }

    @Override
    public long entitySerialId() {
        return SlackDistributionConfigEntity.getSerialversionuid();
    }

    @Override
    public int emptyEntityHashCode() {
        return -1152428859;
    }

    @Override
    public void assertEntityFieldsFull(final SlackDistributionConfigEntity entity) {
        assertEquals(mockUtils.getWebhook(), entity.getWebhook());
        assertEquals(mockUtils.getChannelName(), entity.getChannelName());
        assertEquals(mockUtils.getChannelUsername(), entity.getChannelUsername());
    }

    @Override
    public int entityHashCode() {
        return 584468116;
    }

    @Override
    public Class<SlackDistributionConfigEntity> getEntityClass() {
        return SlackDistributionConfigEntity.class;
    }

}

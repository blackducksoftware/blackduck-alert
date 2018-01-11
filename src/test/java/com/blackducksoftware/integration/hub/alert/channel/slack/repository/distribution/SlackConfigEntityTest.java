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
package com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.channel.slack.mock.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EntityTest;

public class SlackConfigEntityTest extends EntityTest<SlackDistributionConfigEntity> {

    @Override
    public MockSlackEntity getMockUtil() {
        return new MockSlackEntity();
    }

    @Override
    public void assertEntityFieldsNull(final SlackDistributionConfigEntity entity) {
        assertNull(entity.getWebhook());
        assertNull(entity.getChannelName());
        assertEquals("Hub-alert", entity.getChannelUsername());
    }

    @Override
    public void assertEntityFieldsFull(final SlackDistributionConfigEntity entity) {
        assertEquals(getMockUtil().getWebhook(), entity.getWebhook());
        assertEquals(getMockUtil().getChannelName(), entity.getChannelName());
        assertEquals(getMockUtil().getChannelUsername(), entity.getChannelUsername());
    }

    @Override
    public Class<SlackDistributionConfigEntity> getEntityClass() {
        return SlackDistributionConfigEntity.class;
    }

}

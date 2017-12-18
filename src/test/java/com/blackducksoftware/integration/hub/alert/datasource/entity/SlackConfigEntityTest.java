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
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.SlackMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;

public class SlackConfigEntityTest extends EntityTest<SlackDistributionRestModel, ConfigRestModel, SlackDistributionConfigEntity, GlobalSlackConfigEntity> {
    private static final SlackMockUtils mockUtils = new SlackMockUtils();

    public SlackConfigEntityTest() {
        super(mockUtils, SlackDistributionConfigEntity.class);
    }

    @Override
    public void assertEntityFieldsNull(final SlackDistributionConfigEntity entity) {
        assertNull(entity.getWebhook());
        assertNull(entity.getChannelName());
        assertEquals("Hub-alert", entity.getChannelUsername());
    }

    @Override
    public long emptyEntitySerialId() {
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
}

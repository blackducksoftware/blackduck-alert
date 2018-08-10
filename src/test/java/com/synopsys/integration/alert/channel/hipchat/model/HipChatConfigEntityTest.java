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
package com.synopsys.integration.alert.channel.hipchat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.EntityTest;

public class HipChatConfigEntityTest extends EntityTest<HipChatDistributionConfigEntity> {

    @Override
    public MockHipChatEntity getMockUtil() {
        return new MockHipChatEntity();
    }

    @Override
    public Class<HipChatDistributionConfigEntity> getEntityClass() {
        return HipChatDistributionConfigEntity.class;
    }

    @Override
    public void assertEntityFieldsNull(final HipChatDistributionConfigEntity entity) {
        assertNull(entity.getRoomId());
        assertNull(entity.getColor());
        assertNull(entity.getNotify());
    }

    @Override
    public void assertEntityFieldsFull(final HipChatDistributionConfigEntity entity) {
        assertEquals(getMockUtil().getRoomId(), entity.getRoomId());
        assertEquals(getMockUtil().isNotify(), entity.getNotify());
        assertEquals(getMockUtil().getColor(), entity.getColor());
    }

}

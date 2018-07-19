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
package com.blackducksoftware.integration.alert.channel.hipchat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.GlobalEntityTest;

public class GlobalHipChatConfigEntityTest extends GlobalEntityTest<HipChatGlobalConfigEntity> {

    @Override
    public void assertGlobalEntityFieldsNull(final HipChatGlobalConfigEntity entity) {
        assertNull(entity.getApiKey());
    }

    @Override
    public void assertGlobalEntityFieldsFull(final HipChatGlobalConfigEntity entity) {
        assertEquals(getMockUtil().getApiKey(), entity.getApiKey());
        assertEquals(getMockUtil().getHostServer(), entity.getHostServer());
    }

    @Override
    public Class<HipChatGlobalConfigEntity> getGlobalEntityClass() {
        return HipChatGlobalConfigEntity.class;
    }

    @Override
    public MockHipChatGlobalEntity getMockUtil() {
        return new MockHipChatGlobalEntity();
    }
}

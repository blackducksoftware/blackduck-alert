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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalEntityTest;

public class GlobalHipChatConfigEntityTest extends GlobalEntityTest<GlobalHipChatConfigEntity> {

    @Override
    public void assertGlobalEntityFieldsNull(final GlobalHipChatConfigEntity entity) {
        assertNull(entity.getApiKey());
    }

    @Override
    public void assertGlobalEntityFieldsFull(final GlobalHipChatConfigEntity entity) {
        assertEquals(getMockUtil().getApiKey(), entity.getApiKey());
        assertEquals(getMockUtil().getHostServer(), entity.getHostServer());
    }

    @Override
    public Class<GlobalHipChatConfigEntity> getGlobalEntityClass() {
        return GlobalHipChatConfigEntity.class;
    }

    @Override
    public MockHipChatGlobalEntity getMockUtil() {
        return new MockHipChatGlobalEntity();
    }
}

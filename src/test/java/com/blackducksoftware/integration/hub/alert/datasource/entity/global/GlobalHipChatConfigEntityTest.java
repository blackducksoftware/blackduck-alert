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

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;

public class GlobalHipChatConfigEntityTest extends GlobalEntityTest<HipChatDistributionRestModel, GlobalHipChatConfigRestModel, HipChatDistributionConfigEntity, GlobalHipChatConfigEntity> {
    private static final HipChatMockUtils mockUtils = new HipChatMockUtils();

    public GlobalHipChatConfigEntityTest() {
        super(mockUtils, GlobalHipChatConfigEntity.class);
    }

    @Override
    public void assertGlobalEntityFieldsNull(final GlobalHipChatConfigEntity entity) {
        assertNull(entity.getApiKey());
    }

    @Override
    public long emptyGlobalEntitySerialId() {
        return GlobalHipChatConfigEntity.getSerialversionuid();
    }

    @Override
    public int emptyGlobalEntityHashCode() {
        return 23273;
    }

    @Override
    public void assertGlobalEntityFieldsFull(final GlobalHipChatConfigEntity entity) {
        assertEquals(mockUtils.getApiKey(), entity.getApiKey());
    }

    @Override
    public int globalEntityHashCode() {
        return -215716445;
    }
}

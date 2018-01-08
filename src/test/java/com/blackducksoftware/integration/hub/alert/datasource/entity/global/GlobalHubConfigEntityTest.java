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

import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalEntityTest;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalHubEntity;

public class GlobalHubConfigEntityTest extends GlobalEntityTest<GlobalHubConfigEntity> {

    @Override
    public Class<GlobalHubConfigEntity> getGlobalEntityClass() {
        return GlobalHubConfigEntity.class;
    }

    @Override
    public void assertGlobalEntityFieldsNull(final GlobalHubConfigEntity entity) {
        assertNull(entity.getHubTimeout());
        assertNull(entity.getHubApiKey());
    }

    @Override
    public long globalEntitySerialId() {
        return GlobalHubConfigEntity.getSerialversionuid();
    }

    @Override
    public int emptyGlobalEntityHashCode() {
        return 23273;
    }

    @Override
    public void assertGlobalEntityFieldsFull(final GlobalHubConfigEntity entity) {
        assertEquals(getMockUtil().getHubTimeout(), entity.getHubTimeout());
        assertEquals(getMockUtil().getHubApiKey(), entity.getHubApiKey());
    }

    @Override
    public int globalEntityHashCode() {
        return 39702;
    }

    @Override
    public MockGlobalHubEntity getMockUtil() {
        return new MockGlobalHubEntity();
    }
}

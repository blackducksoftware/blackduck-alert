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

import com.blackducksoftware.integration.hub.alert.mock.GlobalHubMockUtils;
import com.blackducksoftware.integration.hub.alert.mock.MockUtils;

public class GlobalHubConfigEntityTest extends GlobalEntityTest<GlobalHubConfigEntity> {
    private final GlobalHubMockUtils mockUtils = new GlobalHubMockUtils();

    @Override
    public MockUtils<?, ?, ?, GlobalHubConfigEntity> getMockUtil() {
        return mockUtils;
    }

    @Override
    public Class<GlobalHubConfigEntity> getGlobalEntityClass() {
        return GlobalHubConfigEntity.class;
    }

    @Override
    public void assertGlobalEntityFieldsNull(final GlobalHubConfigEntity entity) {
        assertNull(entity.getHubPassword());
        assertNull(entity.getHubTimeout());
        assertNull(entity.getHubUsername());
    }

    @Override
    public long globalEntitySerialId() {
        return GlobalHubConfigEntity.getSerialversionuid();
    }

    @Override
    public int emptyGlobalEntityHashCode() {
        return 31860737;
    }

    @Override
    public void assertGlobalEntityFieldsFull(final GlobalHubConfigEntity entity) {
        assertEquals(mockUtils.getHubPassword(), entity.getHubPassword());
        assertEquals(Integer.valueOf(mockUtils.getHubTimeout()), entity.getHubTimeout());
        assertEquals(mockUtils.getHubUsername(), entity.getHubUsername());
    }

    @Override
    public int globalEntityHashCode() {
        return -593750095;
    }
}

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
package com.blackducksoftware.integration.hub.alert.web.model.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;

public class GlobalHipChatConfigRestModelTest extends GlobalRestModelTest<GlobalHipChatConfigRestModel> {
    private final static HipChatMockUtils mockUtils = new HipChatMockUtils();

    public GlobalHipChatConfigRestModelTest() {
        super(mockUtils, GlobalHipChatConfigRestModel.class);
    }

    @Override
    public void assertGlobalRestModelFieldsNull(final GlobalHipChatConfigRestModel restModel) {
        assertNull(restModel.getApiKey());
    }

    @Override
    public long globalRestModelSerialId() {
        return GlobalHipChatConfigRestModel.getSerialversionuid();
    }

    @Override
    public int emptyGlobalRestModelHashCode() {
        return 23273;
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final GlobalHipChatConfigRestModel restModel) {
        assertEquals(mockUtils.getApiKey(), restModel.getApiKey());
    }

    @Override
    public int globalRestModelHashCode() {
        return -215716397;
    }
}

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

import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.GlobalRestModelTest;

public class GlobalHipChatConfigRestModelTest extends GlobalRestModelTest<HipChatGlobalConfigRestModel> {

    @Override
    public void assertGlobalRestModelFieldsNull(final HipChatGlobalConfigRestModel restModel) {
        assertNull(restModel.getApiKey());
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final HipChatGlobalConfigRestModel restModel) {
        assertEquals(getMockUtil().getApiKey(), restModel.getApiKey());
        assertEquals(getMockUtil().isApiKeyIsSet(), restModel.isApiKeyIsSet());
    }

    @Override
    public Class<HipChatGlobalConfigRestModel> getGlobalRestModelClass() {
        return HipChatGlobalConfigRestModel.class;
    }

    @Override
    public MockHipChatGlobalRestModel getMockUtil() {
        return new MockHipChatGlobalRestModel();
    }
}

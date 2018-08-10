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

import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.synopsys.integration.alert.web.channel.model.HipChatGlobalConfig;
import com.synopsys.integration.alert.web.model.GlobalRestModelTest;

public class GlobalHipChatConfigRestModelTest extends GlobalRestModelTest<HipChatGlobalConfig> {

    @Override
    public void assertGlobalRestModelFieldsNull(final HipChatGlobalConfig restModel) {
        assertNull(restModel.getApiKey());
    }

    @Override
    public void assertGlobalRestModelFieldsFull(final HipChatGlobalConfig restModel) {
        assertEquals(getMockUtil().getApiKey(), restModel.getApiKey());
        assertEquals(getMockUtil().isApiKeyIsSet(), restModel.isApiKeyIsSet());
    }

    @Override
    public Class<HipChatGlobalConfig> getGlobalRestModelClass() {
        return HipChatGlobalConfig.class;
    }

    @Override
    public MockHipChatGlobalRestModel getMockUtil() {
        return new MockHipChatGlobalRestModel();
    }
}

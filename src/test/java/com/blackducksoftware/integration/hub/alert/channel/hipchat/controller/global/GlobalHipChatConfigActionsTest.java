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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global;

import static org.junit.Assert.assertTrue;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatManager;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.global.GlobalActionsTest;

public class GlobalHipChatConfigActionsTest extends GlobalActionsTest<GlobalHipChatConfigRestModel, GlobalHipChatConfigEntity, GlobalHipChatRepositoryWrapper, GlobalHipChatConfigActions> {

    @Override
    public GlobalHipChatConfigActions getMockedConfigActions() {
        final GlobalHipChatRepositoryWrapper hipChatRepo = Mockito.mock(GlobalHipChatRepositoryWrapper.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        final HipChatManager hipChatManager = Mockito.mock(HipChatManager.class);
        return new GlobalHipChatConfigActions(hipChatRepo, objectTransformer, hipChatManager);
    }

    @Override
    public GlobalHipChatConfigActions createMockedConfigActionsUsingObjectTransformer(final ObjectTransformer objectTransformer) {
        final GlobalHipChatRepositoryWrapper hipChatRepo = Mockito.mock(GlobalHipChatRepositoryWrapper.class);
        final HipChatManager hipChatManager = Mockito.mock(HipChatManager.class);
        return new GlobalHipChatConfigActions(hipChatRepo, objectTransformer, hipChatManager);
    }

    @Override
    public Class<GlobalHipChatConfigEntity> getGlobalEntityClass() {
        return GlobalHipChatConfigEntity.class;
    }

    @Override
    public void testConfigurationChangeTriggers() {
    }

    @Override
    public MockHipChatGlobalEntity getGlobalEntityMockUtil() {
        return new MockHipChatGlobalEntity();
    }

    @Override
    public MockHipChatGlobalRestModel getGlobalRestModelMockUtil() {
        return new MockHipChatGlobalRestModel();
    }

    @Override
    public void testInvalidConfig() {
        assertTrue(true);
    }

}

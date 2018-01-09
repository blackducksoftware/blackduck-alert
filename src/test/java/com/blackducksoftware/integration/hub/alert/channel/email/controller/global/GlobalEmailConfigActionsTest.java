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
package com.blackducksoftware.integration.hub.alert.channel.email.controller.global;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailGlobalRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.global.GlobalActionsTest;

public class GlobalEmailConfigActionsTest extends GlobalActionsTest<GlobalEmailConfigRestModel, GlobalEmailConfigEntity, GlobalEmailRepositoryWrapper, GlobalEmailConfigActions> {

    @Override
    public GlobalEmailConfigActions getMockedConfigActions() {
        final GlobalEmailRepositoryWrapper repository = Mockito.mock(GlobalEmailRepositoryWrapper.class);
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        return new GlobalEmailConfigActions(repository, objectTransformer);
    }

    @Override
    public GlobalEmailConfigActions createMockedConfigActionsUsingObjectTransformer(final ObjectTransformer objectTransformer) {
        final GlobalEmailRepositoryWrapper repository = Mockito.mock(GlobalEmailRepositoryWrapper.class);
        return new GlobalEmailConfigActions(repository, objectTransformer);
    }

    @Override
    public Class<GlobalEmailConfigEntity> getGlobalEntityClass() {
        return GlobalEmailConfigEntity.class;
    }

    @Override
    public void testConfigurationChangeTriggers() {
    }

    @Override
    public MockEmailGlobalEntity getGlobalEntityMockUtil() {
        return new MockEmailGlobalEntity();
    }

    @Override
    public MockEmailGlobalRestModel getGlobalRestModelMockUtil() {
        return new MockEmailGlobalRestModel();
    }

}

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
package com.blackducksoftware.integration.hub.alert.web.actions.global;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.mock.HipChatMockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHipChatConfigRestModel;

public class GlobalHipChatConfigActionsTest extends GlobalActionsTest<HipChatDistributionRestModel, GlobalHipChatConfigRestModel, HipChatDistributionConfigEntity, GlobalHipChatConfigEntity, GlobalHipChatConfigActions> {
    private static final HipChatMockUtils mockUtils = new HipChatMockUtils();

    public GlobalHipChatConfigActionsTest() {
        super(mockUtils);
    }

    @Override
    public GlobalHipChatConfigActions getConfigActions() {
        final GlobalHipChatRepository hipChatRepo = Mockito.mock(GlobalHipChatRepository.class);
        final ObjectTransformer objectTransfomer = new ObjectTransformer();
        return new GlobalHipChatConfigActions(hipChatRepo, objectTransfomer);
    }

    @Override
    public GlobalHipChatConfigActions getSaveTestConfigActions() {
        return getConfigActions();
    }

    @Override
    public Class<GlobalHipChatConfigEntity> getGlobalEntityClass() {
        return GlobalHipChatConfigEntity.class;
    }

    @Override
    public void testConfigurationChangeTriggers() {
    }

}

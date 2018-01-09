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
package com.blackducksoftware.integration.hub.alert.web.actions.distribution;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatManager;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockHipChatEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;

public class HipChatConfigActionsTest extends ActionsTest<HipChatDistributionRestModel, HipChatDistributionConfigEntity, HipChatDistributionRepositoryWrapper, HipChatDistributionConfigActions> {

    @Override
    public HipChatDistributionConfigActions getMockedConfigActions() {
        return createMockedConfigActionsUsingObjectTransformer(new ObjectTransformer());
    }

    @Override
    public Class<HipChatDistributionConfigEntity> getConfigEntityClass() {
        return HipChatDistributionConfigEntity.class;
    }

    @Override
    public HipChatDistributionConfigActions createMockedConfigActionsUsingObjectTransformer(final ObjectTransformer objectTransformer) {
        final HipChatManager hipChatManager = Mockito.mock(HipChatManager.class);
        final HipChatDistributionRepositoryWrapper mockedHipChatRepository = Mockito.mock(HipChatDistributionRepositoryWrapper.class);
        final CommonDistributionRepositoryWrapper commonRepository = Mockito.mock(CommonDistributionRepositoryWrapper.class);
        final ConfiguredProjectsRepositoryWrapper projectsRepository = Mockito.mock(ConfiguredProjectsRepositoryWrapper.class);
        final DistributionProjectRepositoryWrapper distributionProjectRepository = Mockito.mock(DistributionProjectRepositoryWrapper.class);
        final ConfiguredProjectsActions<HipChatDistributionRestModel> projectsAction = new ConfiguredProjectsActions<>(projectsRepository, distributionProjectRepository);
        final NotificationTypeRepositoryWrapper notificationRepository = Mockito.mock(NotificationTypeRepositoryWrapper.class);
        final DistributionNotificationTypeRepositoryWrapper notificationDistributionRepository = Mockito.mock(DistributionNotificationTypeRepositoryWrapper.class);
        final NotificationTypesActions<HipChatDistributionRestModel> notificationAction = new NotificationTypesActions<>(notificationRepository, notificationDistributionRepository);
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(commonRepository, mockedHipChatRepository, projectsAction, notificationAction, objectTransformer, hipChatManager);
        return configActions;
    }

    @Override
    public MockHipChatEntity getEntityMockUtil() {
        return new MockHipChatEntity();
    }

    @Override
    public MockHipChatRestModel getRestMockUtil() {
        return new MockHipChatRestModel();
    }
}

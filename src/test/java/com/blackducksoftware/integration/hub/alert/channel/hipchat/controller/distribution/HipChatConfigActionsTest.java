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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.actions.distribution.ActionsTest;

public class HipChatConfigActionsTest extends ActionsTest<HipChatDistributionRestModel, HipChatDistributionConfigEntity, HipChatDistributionRepository, HipChatDistributionConfigActions> {

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
        final DistributionChannelManager manager = Mockito.mock(DistributionChannelManager.class);
        final HipChatDistributionRepository mockedHipChatRepository = Mockito.mock(HipChatDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);
        final ConfiguredProjectsRepository projectsRepository = Mockito.mock(ConfiguredProjectsRepository.class);
        final DistributionProjectRepository distributionProjectRepository = Mockito.mock(DistributionProjectRepository.class);
        final ConfiguredProjectsActions<HipChatDistributionRestModel> projectsAction = new ConfiguredProjectsActions<>(projectsRepository, distributionProjectRepository);
        final NotificationTypeRepository notificationRepository = Mockito.mock(NotificationTypeRepository.class);
        final DistributionNotificationTypeRepository notificationDistributionRepository = Mockito.mock(DistributionNotificationTypeRepository.class);
        final NotificationTypesActions<HipChatDistributionRestModel> notificationAction = new NotificationTypesActions<>(notificationRepository, notificationDistributionRepository);
        final HipChatDistributionConfigActions configActions = new HipChatDistributionConfigActions(commonRepository, mockedHipChatRepository, projectsAction, notificationAction, objectTransformer, manager);
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

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
package com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailRestModel;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepository;
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

public class EmailConfigActionsTest extends ActionsTest<EmailGroupDistributionRestModel, EmailGroupDistributionConfigEntity, EmailGroupDistributionRepository, EmailGroupDistributionConfigActions> {

    @Override
    public EmailGroupDistributionConfigActions getMockedConfigActions() {
        return createMockedConfigActionsUsingObjectTransformer(new ObjectTransformer());
    }

    @Override
    public Class<EmailGroupDistributionConfigEntity> getConfigEntityClass() {
        return EmailGroupDistributionConfigEntity.class;
    }

    @Override
    public EmailGroupDistributionConfigActions createMockedConfigActionsUsingObjectTransformer(final ObjectTransformer objectTransformer) {
        final DistributionChannelManager manager = Mockito.mock(DistributionChannelManager.class);
        final EmailGroupDistributionRepository mockedEmailRepository = Mockito.mock(EmailGroupDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);
        final ConfiguredProjectsRepository projectsRepository = Mockito.mock(ConfiguredProjectsRepository.class);
        final DistributionProjectRepository distributionProjectRepository = Mockito.mock(DistributionProjectRepository.class);
        final ConfiguredProjectsActions<EmailGroupDistributionRestModel> projectsAction = new ConfiguredProjectsActions<>(projectsRepository, distributionProjectRepository);
        final NotificationTypeRepository notificationRepository = Mockito.mock(NotificationTypeRepository.class);
        final DistributionNotificationTypeRepository notificationDistributionRepository = Mockito.mock(DistributionNotificationTypeRepository.class);
        final NotificationTypesActions<EmailGroupDistributionRestModel> notificationAction = new NotificationTypesActions<>(notificationRepository, notificationDistributionRepository);
        final EmailGroupDistributionConfigActions emailGroupDistributionConfigActions = new EmailGroupDistributionConfigActions(commonRepository, mockedEmailRepository, projectsAction, notificationAction, objectTransformer, manager);
        return emailGroupDistributionConfigActions;
    }

    @Override
    public MockEmailEntity getEntityMockUtil() {
        return new MockEmailEntity();
    }

    @Override
    public MockEmailRestModel getRestMockUtil() {
        return new MockEmailRestModel();
    }

}

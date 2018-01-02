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

import com.blackducksoftware.integration.hub.alert.channel.email.EmailGroupManager;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.EmailGroupDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.mock.EmailMockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.EmailGroupDistributionRestModel;

public class EmailConfigActionsTest extends ActionsTest<EmailGroupDistributionRestModel, EmailGroupDistributionConfigEntity, EmailGroupDistributionRepositoryWrapper, EmailGroupDistributionConfigActions> {
    private static final EmailMockUtils mockUtils = new EmailMockUtils();

    public EmailConfigActionsTest() {
        super(mockUtils);
    }

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
        final EmailGroupManager emailManager = Mockito.mock(EmailGroupManager.class);
        final EmailGroupDistributionRepositoryWrapper mockedEmailRepository = Mockito.mock(EmailGroupDistributionRepositoryWrapper.class);
        final CommonDistributionRepositoryWrapper commonRepository = Mockito.mock(CommonDistributionRepositoryWrapper.class);
        final ConfiguredProjectsRepository projectsRepository = Mockito.mock(ConfiguredProjectsRepository.class);
        final DistributionProjectRepository distributionProjectRepository = Mockito.mock(DistributionProjectRepository.class);
        final ConfiguredProjectsActions<EmailGroupDistributionRestModel> projectsAction = new ConfiguredProjectsActions<>(projectsRepository, distributionProjectRepository);
        final NotificationTypeRepository notificationRepository = Mockito.mock(NotificationTypeRepository.class);
        final DistributionNotificationTypeRepository notificationDistributionRepository = Mockito.mock(DistributionNotificationTypeRepository.class);
        final NotificationTypesActions<EmailGroupDistributionRestModel> notificationAction = new NotificationTypesActions<>(notificationRepository, notificationDistributionRepository);
        final EmailGroupDistributionConfigActions emailGroupDistributionConfigActions = new EmailGroupDistributionConfigActions(commonRepository, mockedEmailRepository, projectsAction, notificationAction, objectTransformer, emailManager);
        return emailGroupDistributionConfigActions;
    }

}

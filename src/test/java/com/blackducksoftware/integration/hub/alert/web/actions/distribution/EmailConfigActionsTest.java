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
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.EmailGroupDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepositoryWrapper;
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
        final ConfiguredProjectsRepositoryWrapper projectsRepository = Mockito.mock(ConfiguredProjectsRepositoryWrapper.class);
        final DistributionProjectRepositoryWrapper distributionProjectRepository = Mockito.mock(DistributionProjectRepositoryWrapper.class);
        final ConfiguredProjectsActions<EmailGroupDistributionRestModel> projectsAction = new ConfiguredProjectsActions<>(projectsRepository, distributionProjectRepository);
        final NotificationTypeRepositoryWrapper notificationRepository = Mockito.mock(NotificationTypeRepositoryWrapper.class);
        final DistributionNotificationTypeRepositoryWrapper notificationDistributionRepository = Mockito.mock(DistributionNotificationTypeRepositoryWrapper.class);
        final NotificationTypesActions<EmailGroupDistributionRestModel> notificationAction = new NotificationTypesActions<>(notificationRepository, notificationDistributionRepository);
        final EmailGroupDistributionConfigActions emailGroupDistributionConfigActions = new EmailGroupDistributionConfigActions(commonRepository, mockedEmailRepository, projectsAction, notificationAction, objectTransformer, emailManager);
        return emailGroupDistributionConfigActions;
    }

}

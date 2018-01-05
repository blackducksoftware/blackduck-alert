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

import com.blackducksoftware.integration.hub.alert.channel.slack.SlackManager;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.NotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.SlackDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionNotificationTypeRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.mock.SlackMockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.ConfiguredProjectsActions;
import com.blackducksoftware.integration.hub.alert.web.actions.NotificationTypesActions;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;

public class SlackConfigActionsTest extends ActionsTest<SlackDistributionRestModel, SlackDistributionConfigEntity, SlackDistributionRepositoryWrapper, SlackDistributionConfigActions> {
    private static final SlackMockUtils mockUtils = new SlackMockUtils();

    public SlackConfigActionsTest() {
        super(mockUtils);
    }

    @Override
    public SlackDistributionConfigActions getMockedConfigActions() {
        return createMockedConfigActionsUsingObjectTransformer(new ObjectTransformer());
    }

    @Override
    public Class<SlackDistributionConfigEntity> getConfigEntityClass() {
        return SlackDistributionConfigEntity.class;
    }

    @Override
    public SlackDistributionConfigActions createMockedConfigActionsUsingObjectTransformer(final ObjectTransformer objectTransformer) {
        final SlackManager slackManager = Mockito.mock(SlackManager.class);
        final SlackDistributionRepositoryWrapper mockedSlackRepository = Mockito.mock(SlackDistributionRepositoryWrapper.class);
        final CommonDistributionRepositoryWrapper commonRepository = Mockito.mock(CommonDistributionRepositoryWrapper.class);
        final ConfiguredProjectsRepositoryWrapper projectsRepository = Mockito.mock(ConfiguredProjectsRepositoryWrapper.class);
        final DistributionProjectRepositoryWrapper distributionProjectRepository = Mockito.mock(DistributionProjectRepositoryWrapper.class);
        final ConfiguredProjectsActions<SlackDistributionRestModel> projectsAction = new ConfiguredProjectsActions<>(projectsRepository, distributionProjectRepository);
        final NotificationTypeRepositoryWrapper notificationRepository = Mockito.mock(NotificationTypeRepositoryWrapper.class);
        final DistributionNotificationTypeRepositoryWrapper notificationDistributionRepository = Mockito.mock(DistributionNotificationTypeRepositoryWrapper.class);
        final NotificationTypesActions<SlackDistributionRestModel> notificationAction = new NotificationTypesActions<>(notificationRepository, notificationDistributionRepository);
        final SlackDistributionConfigActions configActions = new SlackDistributionConfigActions(commonRepository, mockedSlackRepository, projectsAction, notificationAction, objectTransformer, slackManager);
        return configActions;
    }

}

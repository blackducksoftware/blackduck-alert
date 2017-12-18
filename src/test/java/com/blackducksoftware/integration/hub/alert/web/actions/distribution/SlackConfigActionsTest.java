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

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.channel.slack.SlackChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.ConfiguredProjectsRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.datasource.relation.repository.DistributionProjectRepository;
import com.blackducksoftware.integration.hub.alert.mock.SlackMockUtils;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;
import com.google.gson.Gson;

public class SlackConfigActionsTest extends ActionsTest<SlackDistributionRestModel, ConfigRestModel, SlackDistributionConfigEntity, GlobalSlackConfigEntity, SlackDistributionConfigActions> {
    private static final SlackMockUtils mockUtils = new SlackMockUtils();

    public SlackConfigActionsTest() {
        super(mockUtils);
    }

    @Override
    public SlackDistributionConfigActions getConfigActions() {
        final SlackDistributionRepository mockedHipChatRepository = Mockito.mock(SlackDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);
        final ConfiguredProjectsRepository projectsRepository = Mockito.mock(ConfiguredProjectsRepository.class);
        final DistributionProjectRepository distributionProjectRepository = Mockito.mock(DistributionProjectRepository.class);
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final SlackChannel hipChatChannel = new SlackChannel(new Gson(), null, null, globalProperties);
        final SlackDistributionConfigActions configActions = new SlackDistributionConfigActions(commonRepository, projectsRepository, distributionProjectRepository, mockedHipChatRepository, new ObjectTransformer(), hipChatChannel);
        return configActions;
    }

    @Override
    public Class<SlackDistributionConfigEntity> getConfigEntityClass() {
        return SlackDistributionConfigEntity.class;
    }

    @Override
    public SlackDistributionConfigActions createConfigActionsWithSpecificObjectTransformer(final ObjectTransformer objectTransformer) {
        final SlackDistributionRepository mockedHipChatRepository = Mockito.mock(SlackDistributionRepository.class);
        final CommonDistributionRepository commonRepository = Mockito.mock(CommonDistributionRepository.class);
        final ConfiguredProjectsRepository projectsRepository = Mockito.mock(ConfiguredProjectsRepository.class);
        final DistributionProjectRepository distributionProjectRepository = Mockito.mock(DistributionProjectRepository.class);
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final TestGlobalProperties globalProperties = new TestGlobalProperties(mockedGlobalRepository);
        final SlackChannel hipChatChannel = new SlackChannel(new Gson(), null, null, globalProperties);
        final SlackDistributionConfigActions configActions = new SlackDistributionConfigActions(commonRepository, projectsRepository, distributionProjectRepository, mockedHipChatRepository, objectTransformer, hipChatChannel);
        return configActions;
    }

}

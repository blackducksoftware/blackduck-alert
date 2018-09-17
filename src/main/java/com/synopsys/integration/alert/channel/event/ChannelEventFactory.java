/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.event;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannelEvent;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannelEvent;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.SlackChannelEvent;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.rest.RestConstants;

@Component
public class ChannelEventFactory {
    private final EmailGroupDistributionRepository emailGroupDistributionRepository;
    private final HipChatDistributionRepository hipChatDistributionRepository;
    private final SlackDistributionRepository slackDistributionRepository;

    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;

    @Autowired
    public ChannelEventFactory(final EmailGroupDistributionRepository emailGroupDistributionRepository, final HipChatDistributionRepository hipChatDistributionRepository,
        final SlackDistributionRepository slackDistributionRepository, final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor) {
        this.emailGroupDistributionRepository = emailGroupDistributionRepository;
        this.hipChatDistributionRepository = hipChatDistributionRepository;
        this.slackDistributionRepository = slackDistributionRepository;

        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
    }

    public ChannelEvent createChannelEvent(final Long commonDistributionConfigId, final String destination, final NotificationContent notificationContent) {
        if (destination.equals(EmailGroupChannel.COMPONENT_NAME)) {
            final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = emailGroupDistributionRepository.getOne(commonDistributionConfigId);
            return createEmailEvent(commonDistributionConfigId, emailGroupDistributionConfigEntity, notificationContent);
        } else if (destination.equals(HipChatChannel.COMPONENT_NAME)) {
            final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = hipChatDistributionRepository.getOne(commonDistributionConfigId);
            return createHipChatChannelEvent(commonDistributionConfigId, hipChatDistributionConfigEntity, notificationContent);
        } else if (destination.equals(SlackChannel.COMPONENT_NAME)) {
            final SlackDistributionConfigEntity slackDistributionConfigEntity = slackDistributionRepository.getOne(commonDistributionConfigId);
            return createSlackChannelEvent(commonDistributionConfigId, slackDistributionConfigEntity, notificationContent);
        }
        return null;
    }

    public SlackChannelEvent createSlackChannelEvent(final Long commonDistributionConfigId, final SlackDistributionConfigEntity slackDistributionConfigEntity, final NotificationContent notificationContent) {
        return new SlackChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(), notificationContent.getContent(),
            notificationContent.getId(), commonDistributionConfigId,
            slackDistributionConfigEntity.getChannelUsername(), slackDistributionConfigEntity.getWebhook(), slackDistributionConfigEntity.getChannelName());
    }

    public SlackChannelEvent createSlackChannelTestEvent(final Config restModel) {
        final NotificationContent notificationContent = createTestNotificationContent();
        final SlackDistributionConfig slackDistributionConfig = (SlackDistributionConfig) restModel;
        return new SlackChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(), notificationContent.getContent(),
            notificationContent.getId(), null, slackDistributionConfig.getChannelUsername(), slackDistributionConfig.getWebhook(), slackDistributionConfig.getChannelName());
    }

    public HipChatChannelEvent createHipChatChannelEvent(final Long commonDistributionConfigId, final HipChatDistributionConfigEntity hipChatDistributionConfigEntity, final NotificationContent notificationContent) {
        return new HipChatChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(), notificationContent.getContent(),
            notificationContent.getId(), commonDistributionConfigId,
            hipChatDistributionConfigEntity.getRoomId(), hipChatDistributionConfigEntity.getNotify(), hipChatDistributionConfigEntity.getColor());
    }

    public HipChatChannelEvent createHipChatChannelTestEvent(final Config restModel) {
        final NotificationContent notificationContent = createTestNotificationContent();
        final HipChatDistributionConfig hipChatDistributionConfig = (HipChatDistributionConfig) restModel;
        return new HipChatChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(), notificationContent.getContent(),
            notificationContent.getId(), null, NumberUtils.toInt(hipChatDistributionConfig.getRoomId(), 0), hipChatDistributionConfig.getNotify(), hipChatDistributionConfig.getColor());
    }

    public EmailChannelEvent createEmailEvent(final Long commonDistributionConfigId, final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity, final NotificationContent notificationContent) {
        //TODO get projectName from notification content
        final String projectName = null;
        return new EmailChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(), notificationContent.getContent(), notificationContent.getId(),
            commonDistributionConfigId, getEmailAddressesForProject(projectName), emailGroupDistributionConfigEntity.getEmailSubjectLine());
    }

    public EmailChannelEvent createEmailChannelTestEvent(final Config restModel) {
        final NotificationContent notificationContent = createTestNotificationContent();

        final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) restModel;

        final Set<String> emailAddresses = new HashSet<>();
        if (BooleanUtils.toBoolean(emailDistributionConfig.getFilterByProject())) {
            emailDistributionConfig.getConfiguredProjects()
                .stream()
                .forEach(project -> emailAddresses.addAll(getEmailAddressesForProject(project)));

        } else if (emailDistributionConfig.getProviderName().equals(BlackDuckProvider.COMPONENT_NAME)) {
            blackDuckUserRepositoryAccessor.readEntities()
                .stream()
                .map(databaseEntity -> (BlackDuckUserEntity) databaseEntity)
                .forEach(blackDuckUserEntity -> emailAddresses.add(blackDuckUserEntity.getEmailAddress()));
        }
        return new EmailChannelEvent(RestConstants.formatDate(notificationContent.getCreatedAt()), notificationContent.getProvider(), notificationContent.getNotificationType(), notificationContent.getContent(), notificationContent.getId(),
            null, emailAddresses, emailDistributionConfig.getEmailSubjectLine());
    }

    private Set<String> getEmailAddressesForProject(final String projectName) {
        final BlackDuckProjectEntity blackDuckProjectEntity = blackDuckProjectRepositoryAccessor.findByName(projectName);
        final List<UserProjectRelation> userProjectRelations = userProjectRelationRepositoryAccessor.findByBlackDuckProjectId(blackDuckProjectEntity.getId());
        final Set<String> emailAddresses = userProjectRelations
                                               .stream()
                                               .map(userProjectRelation -> blackDuckUserRepositoryAccessor.readEntity(userProjectRelation.getBlackDuckUserId()))
                                               .filter(userEntity -> userEntity.isPresent())
                                               .map(userEntity -> ((BlackDuckUserEntity) userEntity.get()).getEmailAddress())
                                               .collect(Collectors.toSet());
        return emailAddresses;
    }

    private NotificationContent createTestNotificationContent() {
        return new NotificationContent(new Date(), "Alert", "Test Message", "Alert has sent this test message");
    }

}

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

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.EmailChannelEvent;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannelEvent;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.SlackChannelEvent;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
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
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.rest.RestConstants;

@Component
public class ChannelEventFactory {
    private final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor;
    private final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor;
    private final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor;

    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    private final Gson gson;

    @Autowired
    public ChannelEventFactory(final EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor, final HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor,
        final SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor, final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor, final Gson gson) {
        this.emailDistributionRepositoryAccessor = emailDistributionRepositoryAccessor;
        this.hipChatDistributionRepositoryAccessor = hipChatDistributionRepositoryAccessor;
        this.slackDistributionRepositoryAccessor = slackDistributionRepositoryAccessor;

        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
        this.gson = gson;
    }

    public ChannelEvent createChannelEvent(final CommonDistributionConfig config, final AggregateMessageContent messageContent) throws AlertException {
        final Long distributionConfigId = Long.valueOf(config.getDistributionConfigId());
        final String destination = config.getDistributionType();
        if (destination.equals(EmailGroupChannel.COMPONENT_NAME)) {
            final Optional<? extends DatabaseEntity> optionalDatabaseEntity = emailDistributionRepositoryAccessor.readEntity(distributionConfigId);
            if (!optionalDatabaseEntity.isPresent()) {
                throw new AlertException("Could not find the email configuration with Id " + distributionConfigId);
            }
            final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = (EmailGroupDistributionConfigEntity) optionalDatabaseEntity.get();
            return createEmailEvent(config, emailGroupDistributionConfigEntity, messageContent);
        } else if (destination.equals(HipChatChannel.COMPONENT_NAME)) {
            final Optional<? extends DatabaseEntity> optionalDatabaseEntity = hipChatDistributionRepositoryAccessor.readEntity(distributionConfigId);
            if (!optionalDatabaseEntity.isPresent()) {
                throw new AlertException("Could not find the hipchat configuration with Id " + distributionConfigId);
            }
            final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = (HipChatDistributionConfigEntity) optionalDatabaseEntity.get();
            return createHipChatChannelEvent(config, hipChatDistributionConfigEntity, messageContent);
        } else if (destination.equals(SlackChannel.COMPONENT_NAME)) {
            final Optional<? extends DatabaseEntity> optionalDatabaseEntity = slackDistributionRepositoryAccessor.readEntity(distributionConfigId);
            if (!optionalDatabaseEntity.isPresent()) {
                throw new AlertException("Could not find the slack configuration with Id " + distributionConfigId);
            }
            final SlackDistributionConfigEntity slackDistributionConfigEntity = (SlackDistributionConfigEntity) optionalDatabaseEntity.get();
            return createSlackChannelEvent(config, slackDistributionConfigEntity, messageContent);
        }
        return null;
    }

    public SlackChannelEvent createSlackChannelEvent(final CommonDistributionConfig commonDistributionConfig, final SlackDistributionConfigEntity slackDistributionConfigEntity, final AggregateMessageContent messageContent) {
        return new SlackChannelEvent(RestConstants.formatDate(new Date()), commonDistributionConfig.getProviderName(), commonDistributionConfig.getFormatType(), messageContent,
            Long.valueOf(commonDistributionConfig.getId()), slackDistributionConfigEntity.getChannelUsername(), slackDistributionConfigEntity.getWebhook(), slackDistributionConfigEntity.getChannelName());
    }

    public SlackChannelEvent createSlackChannelTestEvent(final Config restModel) {
        final AggregateMessageContent messageContent = createTestNotificationContent();
        final SlackDistributionConfig slackDistributionConfig = (SlackDistributionConfig) restModel;
        return new SlackChannelEvent(RestConstants.formatDate(new Date()), slackDistributionConfig.getProviderName(), slackDistributionConfig.getFormatType(), messageContent,
            null, slackDistributionConfig.getChannelUsername(), slackDistributionConfig.getWebhook(), slackDistributionConfig.getChannelName());
    }

    public HipChatChannelEvent createHipChatChannelEvent(final CommonDistributionConfig commonDistributionConfig, final HipChatDistributionConfigEntity hipChatDistributionConfigEntity, final AggregateMessageContent messageContent) {
        return new HipChatChannelEvent(RestConstants.formatDate(new Date()), commonDistributionConfig.getProviderName(), commonDistributionConfig.getFormatType(), messageContent, Long.valueOf(commonDistributionConfig.getId()),
            hipChatDistributionConfigEntity.getRoomId(), hipChatDistributionConfigEntity.getNotify(), hipChatDistributionConfigEntity.getColor());
    }

    public HipChatChannelEvent createHipChatChannelTestEvent(final Config restModel) {
        final AggregateMessageContent messageContent = createTestNotificationContent();
        final HipChatDistributionConfig hipChatDistributionConfig = (HipChatDistributionConfig) restModel;
        return new HipChatChannelEvent(RestConstants.formatDate(new Date()), hipChatDistributionConfig.getProviderName(), hipChatDistributionConfig.getFormatType(), messageContent,
            null, NumberUtils.toInt(hipChatDistributionConfig.getRoomId(), 0), hipChatDistributionConfig.getNotify(), hipChatDistributionConfig.getColor());
    }

    public EmailChannelEvent createEmailEvent(final CommonDistributionConfig commonDistributionConfig, final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity, final AggregateMessageContent messageContent) {
        final String projectName = messageContent.getValue();
        return new EmailChannelEvent(RestConstants.formatDate(new Date()), commonDistributionConfig.getProviderName(), commonDistributionConfig.getFormatType(), messageContent,
            Long.valueOf(commonDistributionConfig.getId()), getEmailAddressesForProject(projectName), emailGroupDistributionConfigEntity.getEmailSubjectLine());
    }

    public EmailChannelEvent createEmailChannelTestEvent(final Config restModel) {
        final AggregateMessageContent messageContent = createTestNotificationContent();

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
        return new EmailChannelEvent(RestConstants.formatDate(new Date()), emailDistributionConfig.getProviderName(), emailDistributionConfig.getFormatType(), messageContent,
            null, emailAddresses, emailDistributionConfig.getEmailSubjectLine());
    }

    private Set<String> getEmailAddressesForProject(final String projectName) {
        if (StringUtils.isBlank(projectName)) {
            return Collections.emptySet();
        }
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

    private AggregateMessageContent createTestNotificationContent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        return new AggregateMessageContent("testTopic", "Alert Test Message", null, subTopic, Collections.emptyList());

    }

}

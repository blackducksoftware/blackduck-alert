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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannelEvent;
import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannelEvent;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.SlackChannelEvent;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
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
import com.synopsys.integration.alert.web.exception.AlertFieldException;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.rest.RestConstants;

@Component
public class ChannelEventFactory {
    private final Logger logger = LoggerFactory.getLogger(ChannelEventFactory.class);

    private final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    private final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    private final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;

    @Autowired
    public ChannelEventFactory(final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor, final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor) {

        this.blackDuckProjectRepositoryAccessor = blackDuckProjectRepositoryAccessor;
        this.blackDuckUserRepositoryAccessor = blackDuckUserRepositoryAccessor;
        this.userProjectRelationRepositoryAccessor = userProjectRelationRepositoryAccessor;
    }

    public ChannelEvent createChannelEvent(final CommonDistributionConfig config, final AggregateMessageContent messageContent) {
        final String distributionType = config.getDistributionType();
        if (distributionType.equals(EmailGroupChannel.COMPONENT_NAME)) {
            final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) config;
            return createEmailEvent(emailDistributionConfig, messageContent);
        } else if (distributionType.equals(HipChatChannel.COMPONENT_NAME)) {
            final HipChatDistributionConfig hipChatDistributionConfig = (HipChatDistributionConfig) config;
            return createHipChatChannelEvent(hipChatDistributionConfig, messageContent);
        } else if (distributionType.equals(SlackChannel.COMPONENT_NAME)) {
            final SlackDistributionConfig slackDistributionConfig = (SlackDistributionConfig) config;
            return createSlackChannelEvent(slackDistributionConfig, messageContent);
        }
        return null;
    }

    public SlackChannelEvent createSlackChannelEvent(final SlackDistributionConfig slackDistributionConfig, final AggregateMessageContent messageContent) {
        return new SlackChannelEvent(RestConstants.formatDate(new Date()), slackDistributionConfig.getProviderName(), slackDistributionConfig.getFormatType(), messageContent,
            Long.valueOf(slackDistributionConfig.getId()), slackDistributionConfig.getChannelUsername(), slackDistributionConfig.getWebhook(), slackDistributionConfig.getChannelName());
    }

    public SlackChannelEvent createSlackChannelTestEvent(final Config restModel) {
        final AggregateMessageContent messageContent = createTestNotificationContent();
        final SlackDistributionConfig slackDistributionConfig = (SlackDistributionConfig) restModel;
        return new SlackChannelEvent(RestConstants.formatDate(new Date()), slackDistributionConfig.getProviderName(), slackDistributionConfig.getFormatType(), messageContent,
            null, slackDistributionConfig.getChannelUsername(), slackDistributionConfig.getWebhook(), slackDistributionConfig.getChannelName());
    }

    public HipChatChannelEvent createHipChatChannelEvent(final HipChatDistributionConfig hipChatDistributionConfig, final AggregateMessageContent messageContent) {
        return new HipChatChannelEvent(RestConstants.formatDate(new Date()), hipChatDistributionConfig.getProviderName(), hipChatDistributionConfig.getFormatType(), messageContent, Long.valueOf(hipChatDistributionConfig.getId()),
            NumberUtils.toInt(hipChatDistributionConfig.getRoomId()), hipChatDistributionConfig.getNotify(), hipChatDistributionConfig.getColor());
    }

    public HipChatChannelEvent createHipChatChannelTestEvent(final Config restModel) {
        final AggregateMessageContent messageContent = createTestNotificationContent();
        final HipChatDistributionConfig hipChatDistributionConfig = (HipChatDistributionConfig) restModel;
        return new HipChatChannelEvent(RestConstants.formatDate(new Date()), hipChatDistributionConfig.getProviderName(), hipChatDistributionConfig.getFormatType(), messageContent,
            null, NumberUtils.toInt(hipChatDistributionConfig.getRoomId(), 0), hipChatDistributionConfig.getNotify(), hipChatDistributionConfig.getColor());
    }

    public EmailChannelEvent createEmailEvent(final EmailDistributionConfig emailGroupDistributionConfig, final AggregateMessageContent messageContent) {
        final String projectName = messageContent.getValue();
        final BlackDuckProjectEntity projectEntity = blackDuckProjectRepositoryAccessor.findByName(projectName);
        final Set<String> emailAddresses = getEmailAddressesForProject(projectEntity, emailGroupDistributionConfig.getProjectOwnerOnly());
        if (emailAddresses.isEmpty()) {
            logger.error("Could not find any email addresses for project: {}. Job: {}", projectName, emailGroupDistributionConfig.getName());
        }
        return new EmailChannelEvent(RestConstants.formatDate(new Date()), emailGroupDistributionConfig.getProviderName(), emailGroupDistributionConfig.getFormatType(), messageContent,
            Long.valueOf(emailGroupDistributionConfig.getId()), getEmailAddressesForProject(projectEntity, emailGroupDistributionConfig.getProjectOwnerOnly()), emailGroupDistributionConfig.getEmailSubjectLine());

    }

    public EmailChannelEvent createEmailChannelTestEvent(final Config restModel) throws AlertFieldException {
        final AggregateMessageContent messageContent = createTestNotificationContent();

        final EmailDistributionConfig emailDistributionConfig = (EmailDistributionConfig) restModel;

        final Set<String> emailAddresses = new HashSet<>();
        Set<BlackDuckProjectEntity> blackDuckProjectEntities = null;
        if (BooleanUtils.toBoolean(emailDistributionConfig.getFilterByProject())) {
            blackDuckProjectEntities = emailDistributionConfig.getConfiguredProjects()
                                           .stream()
                                           .map(project -> blackDuckProjectRepositoryAccessor.findByName(project))
                                           .collect(Collectors.toSet());
        } else if (emailDistributionConfig.getProviderName().equals(BlackDuckProvider.COMPONENT_NAME)) {
            blackDuckProjectEntities = blackDuckProjectRepositoryAccessor.readEntities()
                                           .stream()
                                           .map(databaseEntity -> (BlackDuckProjectEntity) databaseEntity)
                                           .collect(Collectors.toSet());

        }
        if (null != blackDuckProjectEntities) {
            final Set<String> projectsWithoutEmails = new HashSet<>();
            blackDuckProjectEntities
                .stream()
                .forEach(project -> {
                    final Set<String> emailsForProject = getEmailAddressesForProject(project, emailDistributionConfig.getProjectOwnerOnly());
                    if (emailsForProject.isEmpty()) {
                        projectsWithoutEmails.add(project.getName());
                    }
                    emailAddresses.addAll(emailsForProject);
                });
            if (!projectsWithoutEmails.isEmpty()) {
                final String projects = StringUtils.join(projectsWithoutEmails, ", ");
                final Map<String, String> fieldErrors = new HashMap<>();
                String errorMessage = "";
                if (emailDistributionConfig.getProjectOwnerOnly()) {
                    errorMessage = String.format("Could not find Project owners for the projects: %s", projects);
                } else {
                    errorMessage = String.format("Could not find any email addresses for the projects: %s", projects);
                }
                fieldErrors.put("configuredProjects", errorMessage);
                throw new AlertFieldException(fieldErrors);
            }
        }
        return new EmailChannelEvent(RestConstants.formatDate(new Date()), emailDistributionConfig.getProviderName(), emailDistributionConfig.getFormatType(), messageContent,
            null, emailAddresses, emailDistributionConfig.getEmailSubjectLine());
    }

    private Set<String> getEmailAddressesForProject(final BlackDuckProjectEntity blackDuckProjectEntity, final boolean projectOwnerOnly) {
        if (null == blackDuckProjectEntity) {
            return Collections.emptySet();
        }
        final Set<String> emailAddresses;
        if (projectOwnerOnly) {
            emailAddresses = new HashSet<>();
            if (StringUtils.isNotBlank(blackDuckProjectEntity.getProjectOwnerEmail())) {
                emailAddresses.add(blackDuckProjectEntity.getProjectOwnerEmail());
            }
        } else {
            final List<UserProjectRelation> userProjectRelations = userProjectRelationRepositoryAccessor.findByBlackDuckProjectId(blackDuckProjectEntity.getId());
            emailAddresses = userProjectRelations
                                 .stream()
                                 .map(userProjectRelation -> blackDuckUserRepositoryAccessor.readEntity(userProjectRelation.getBlackDuckUserId()))
                                 .filter(userEntity -> userEntity.isPresent())
                                 .map(databaseEntity -> (BlackDuckUserEntity) databaseEntity.get())
                                 .filter(userEntity -> StringUtils.isNotBlank(userEntity.getEmailAddress()))
                                 .map(userEntity -> userEntity.getEmailAddress())
                                 .collect(Collectors.toSet());
        }
        return emailAddresses;
    }

    private AggregateMessageContent createTestNotificationContent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        return new AggregateMessageContent("testTopic", "Alert Test Message", null, subTopic, Collections.emptyList());

    }

}

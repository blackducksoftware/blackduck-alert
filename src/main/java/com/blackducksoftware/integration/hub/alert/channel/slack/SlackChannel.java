/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.slack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.hub.alert.channel.rest.RestDistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataFactory;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.throwaway.ItemTypeEnum;
import com.blackducksoftware.integration.rest.request.Request;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component(value = SlackChannel.COMPONENT_NAME)
@Transactional
public class SlackChannel extends RestDistributionChannel<GlobalSlackConfigEntity, SlackDistributionConfigEntity> {
    public static final String COMPONENT_NAME = "slack_channel";
    public static final String SLACK_API = "https://hooks.slack.com";

    @Autowired
    public SlackChannel(final Gson gson, final AuditEntryRepository auditEntryRepository, final SlackDistributionRepository slackDistributionRepository, final CommonDistributionRepository commonDistributionRepository,
            final ChannelRestConnectionFactory channelRestConnectionFactory, final AlertEventContentConverter contentExtractor) {
        super(gson, auditEntryRepository, null, slackDistributionRepository, commonDistributionRepository, channelRestConnectionFactory, contentExtractor);
    }

    @Override
    public String getApiUrl(final GlobalSlackConfigEntity globalConfig) {
        return SLACK_API;
    }

    @Override
    public Request createRequest(final ChannelRequestHelper channelRequestHelper, final SlackDistributionConfigEntity config, final GlobalSlackConfigEntity globalConfig, final DigestModel digestModel) throws IntegrationException {
        if (StringUtils.isBlank(config.getWebhook())) {
            throw new IntegrationException("Missing Webhook URL");
        } else if (StringUtils.isBlank(config.getChannelName())) {
            throw new IntegrationException("Missing channel name");
        } else {

            final String slackUrl = config.getWebhook();
            final Collection<ProjectData> projectDataCollection = digestModel.getProjectDataCollection();
            final String htmlMessage = createHtmlMessage(projectDataCollection);
            final String jsonString = getJsonString(htmlMessage, config.getChannelName(), config.getChannelUsername());

            final Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Content-Type", "application/json");

            return channelRequestHelper.createPostMessageRequest(slackUrl, requestHeaders, jsonString);
        }
    }

    private String createHtmlMessage(final Collection<ProjectData> projectDataCollection) {
        final StringBuilder messageBuilder = new StringBuilder();
        projectDataCollection.forEach(projectData -> {
            messageBuilder.append(System.lineSeparator());
            messageBuilder.append(projectData.getProjectName());
            messageBuilder.append(" > ");
            messageBuilder.append(projectData.getProjectVersion());
            messageBuilder.append(System.lineSeparator());

            final Map<NotificationCategoryEnum, CategoryData> categoryMap = projectData.getCategoryMap();
            if (categoryMap != null) {
                for (final NotificationCategoryEnum category : NotificationCategoryEnum.values()) {
                    final CategoryData data = categoryMap.get(category);
                    if (data != null) {
                        messageBuilder.append("- - - - - - - - - - - - - - - - - - - -");
                        messageBuilder.append(System.lineSeparator());
                        messageBuilder.append("Type: ");
                        messageBuilder.append(data.getCategoryKey());
                        messageBuilder.append(System.lineSeparator());
                        messageBuilder.append("Number of Changes: ");
                        messageBuilder.append(data.getItemCount());
                        for (final ItemData item : data.getItems()) {
                            messageBuilder.append(System.lineSeparator());
                            final Map<String, Object> dataSet = item.getDataSet();
                            final String ruleKey = ItemTypeEnum.RULE.toString();
                            if (dataSet.containsKey(ruleKey) && StringUtils.isNotBlank(dataSet.get(ruleKey).toString())) {
                                messageBuilder.append("Rule: " + dataSet.get(ItemTypeEnum.RULE.toString()));
                                messageBuilder.append(System.lineSeparator());
                            }

                            if (dataSet.containsKey(ProjectDataFactory.VULNERABILITY_COUNT_KEY_ADDED)) {
                                final Number numericValue = (Number) dataSet.get(ProjectDataFactory.VULNERABILITY_COUNT_KEY_ADDED);
                                messageBuilder.append("Vulnerability Count Added: " + numericValue.intValue());
                                messageBuilder.append(System.lineSeparator());
                            }

                            if (dataSet.containsKey(ProjectDataFactory.VULNERABILITY_COUNT_KEY_UPDATED)) {
                                final Number numericValue = (Number) dataSet.get(ProjectDataFactory.VULNERABILITY_COUNT_KEY_UPDATED);
                                messageBuilder.append("Vulnerability Count Updated: " + numericValue.intValue());
                                messageBuilder.append(System.lineSeparator());
                            }

                            if (dataSet.containsKey(ProjectDataFactory.VULNERABILITY_COUNT_KEY_DELETED)) {
                                final Number numericValue = (Number) dataSet.get(ProjectDataFactory.VULNERABILITY_COUNT_KEY_DELETED);
                                messageBuilder.append("Vulnerability Count Deleted: " + numericValue.intValue());
                                messageBuilder.append(System.lineSeparator());
                            }

                            messageBuilder.append("Component: " + dataSet.get(ItemTypeEnum.COMPONENT.toString()));
                            messageBuilder.append(" [" + dataSet.get(ItemTypeEnum.VERSION.toString()) + "]");
                        }
                        messageBuilder.append(System.lineSeparator());
                    }
                }
            } else {
                messageBuilder.append(" A notification was received, but it was empty.");
            }
        });
        return messageBuilder.toString();
    }

    private String getJsonString(final String htmlMessage, final String channel, final String username) {
        final JsonObject json = new JsonObject();
        json.addProperty("text", htmlMessage);
        json.addProperty("channel", channel);
        json.addProperty("username", username);
        json.addProperty("mrkdwn", true);

        return json.toString();
    }

}

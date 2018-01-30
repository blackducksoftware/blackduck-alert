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
package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.AlertConstants;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.ChannelFreemarkerTemplatingService;
import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.hub.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.hub.alert.channel.rest.RestDistributionChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import freemarker.template.TemplateException;
import okhttp3.Request;

@Component
public class HipChatChannel extends RestDistributionChannel<HipChatEvent, GlobalHipChatConfigEntity, HipChatDistributionConfigEntity> {
    public static final String HIP_CHAT_API = "https://api.hipchat.com";

    @Autowired
    public HipChatChannel(final Gson gson, final AuditEntryRepositoryWrapper auditEntryRepository, final GlobalHipChatRepositoryWrapper globalHipChatRepository, final CommonDistributionRepositoryWrapper commonDistributionRepository,
            final HipChatDistributionRepositoryWrapper hipChatDistributionRepository, final ChannelRestConnectionFactory channelRestConnectionFactory) {
        super(gson, auditEntryRepository, globalHipChatRepository, hipChatDistributionRepository, commonDistributionRepository, HipChatEvent.class, channelRestConnectionFactory);
    }

    @JmsListener(destination = SupportedChannels.HIPCHAT)
    @Override
    public void receiveMessage(final String message) {
        super.receiveMessage(message);
    }

    @Override
    public String getApiUrl() {
        return HIP_CHAT_API;
    }

    @Override
    public Request createRequest(final ChannelRequestHelper channelRequestHelper, final HipChatDistributionConfigEntity config, final ProjectData projectData) throws IntegrationException {
        final String htmlMessage = createHtmlMessage(projectData);
        final String jsonString = getJsonString(htmlMessage, AlertConstants.ALERT_APPLICATION_NAME, config.getNotify(), config.getColor());

        if (config.getRoomId() == null) {
            throw new IntegrationException("Room ID missing");
        } else {
            final List<String> urlSegments = Arrays.asList("v2", "room", config.getRoomId().toString(), "notification");

            final Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization", "Bearer " + getGlobalConfigEntity().getApiKey());
            requestHeaders.put("Content-Type", "application/json");

            return channelRequestHelper.createMessageRequest(urlSegments, requestHeaders, jsonString);
        }
    }

    private String createHtmlMessage(final ProjectData projectData) {
        try {
            // TODO determine the actual template location for deployment
            final ChannelFreemarkerTemplatingService freemarkerTemplatingService = new ChannelFreemarkerTemplatingService(System.getProperties().getProperty("user.dir") + "/src/main/resources/hipchat/templates");

            final HashMap<String, Object> model = new HashMap<>();
            model.put("projectName", projectData.getProjectName());
            model.put("projectVersion", projectData.getProjectVersion());
            model.put("categoryMap", projectData.getCategoryMap());

            return freemarkerTemplatingService.getResolvedTemplate(model, "notification.ftl");
        } catch (final IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    private String getJsonString(final String htmlMessage, final String from, final boolean notify, final String color) {
        final JsonObject json = new JsonObject();
        json.addProperty("message_format", "html");
        if (htmlMessage != null) {
            json.addProperty("message", htmlMessage);
        }
        if (from != null) {
            json.addProperty("from", from);
        }
        json.addProperty("notify", notify);
        if (color != null) {
            json.addProperty("color", color);
        }
        return json.toString();
    }

}

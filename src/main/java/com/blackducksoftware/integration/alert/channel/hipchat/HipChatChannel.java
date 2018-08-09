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
package com.blackducksoftware.integration.alert.channel.hipchat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.AlertConstants;
import com.blackducksoftware.integration.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.alert.channel.ChannelFreemarkerTemplatingService;
import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.GlobalHipChatRepository;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionRepository;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRequestHelper;
import com.blackducksoftware.integration.alert.channel.rest.ChannelRestConnectionFactory;
import com.blackducksoftware.integration.alert.channel.rest.RestDistributionChannel;
import com.blackducksoftware.integration.alert.config.AlertEnvironment;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import freemarker.template.TemplateException;

@Component(value = HipChatChannel.COMPONENT_NAME)
@Transactional
public class HipChatChannel extends RestDistributionChannel<GlobalHipChatConfigEntity, HipChatDistributionConfigEntity> {
    private static final Logger logger = LoggerFactory.getLogger(HipChatChannel.class);
    public static final String COMPONENT_NAME = "channel_hipchat";
    public static final String HIP_CHAT_API = "https://api.hipchat.com";

    private final ChannelRestConnectionFactory channelRestConnectionFactory;

    @Autowired
    public HipChatChannel(final Gson gson, final GlobalProperties globalProperties, final AuditEntryRepository auditEntryRepository, final GlobalHipChatRepository globalHipChatRepository,
            final CommonDistributionRepository commonDistributionRepository,
            final HipChatDistributionRepository hipChatDistributionRepository, final ChannelRestConnectionFactory channelRestConnectionFactory, final AlertEventContentConverter contentExtractor) {
        super(gson, globalProperties, auditEntryRepository, globalHipChatRepository, hipChatDistributionRepository, commonDistributionRepository, channelRestConnectionFactory, contentExtractor);
        this.channelRestConnectionFactory = channelRestConnectionFactory;
    }

    @Override
    public String getApiUrl(final GlobalHipChatConfigEntity globalConfig) {
        String hipChatHostServer = HIP_CHAT_API;
        final String customHostServer = globalConfig.getHostServer();
        if (!StringUtils.isBlank(customHostServer)) {
            hipChatHostServer = customHostServer;
        }
        return hipChatHostServer;
    }

    @Override
    public String testGlobalConfig(final GlobalHipChatConfigEntity entity) throws IntegrationException {
        if (entity == null) {
            return "The provided entity was null.";
        }
        if (StringUtils.isBlank(entity.getApiKey())) {
            throw new IntegrationException("Invalid API key: API key not provided");
        }
        final RestConnection restConnection = channelRestConnectionFactory.createUnauthenticatedRestConnection(getApiUrl(entity));
        if (restConnection != null) {
            try {
                final String url = getApiUrl(entity) + "/v2/room/*/notification";
                final Map<String, Set<String>> queryParameters = new HashMap<>();
                queryParameters.put("auth_test", new HashSet<>(Arrays.asList("true")));

                final Map<String, String> requestHeaders = new HashMap<>();
                requestHeaders.put("Authorization", "Bearer " + entity.getApiKey());
                requestHeaders.put("Content-Type", "application/json");

                // The {"message":"test"} is required to avoid a BAD_REQUEST (OkHttp issue: #854)
                final ChannelRequestHelper channelRequestHelper = new ChannelRequestHelper(restConnection);
                final Request request = channelRequestHelper.createPostMessageRequest(url, requestHeaders, queryParameters, "{\"message\":\"test\"}");

                final Response response = channelRequestHelper.sendGenericRequest(request);
                if (response.getStatusCode() >= 200 && response.getStatusCode() < 400) {
                    return "API key is valid.";
                }
                return "Invalid API key: " + response.getStatusMessage();
            } catch (final IntegrationException e) {
                logger.error("Unable to create a response", e);
                throw new IntegrationException("Invalid API key: " + e.getMessage());
            } finally {
                try {
                    restConnection.close();
                } catch (final IOException ex) {
                    // close the connection quietly
                }
            }
        }
        return "Connection error: see logs for more information.";
    }

    @Override
    public Request createRequest(final ChannelRequestHelper channelRequestHelper, final HipChatDistributionConfigEntity config, final GlobalHipChatConfigEntity globalConfig, final DigestModel digestModel) throws IntegrationException {
        if (config.getRoomId() == null) {
            throw new IntegrationException("Room ID missing");
        } else {
            final Collection<ProjectData> projectDataCollection = digestModel.getProjectDataCollection();
            final String htmlMessage = createHtmlMessage(projectDataCollection);
            final String jsonString = getJsonString(htmlMessage, AlertConstants.ALERT_APPLICATION_NAME, config.getNotify(), config.getColor());

            final String url = getApiUrl(globalConfig) + "/v2/room/" + config.getRoomId().toString() + "/notification";

            final Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization", "Bearer " + globalConfig.getApiKey());
            requestHeaders.put("Content-Type", "application/json");

            return channelRequestHelper.createPostMessageRequest(url, requestHeaders, jsonString);
        }
    }

    private String createHtmlMessage(final Collection<ProjectData> projectDataCollection) {
        try {
            final String templatesDirectory = getGlobalProperties().getEnvironmentVariable(AlertEnvironment.ALERT_TEMPLATES_DIR);
            final String templateDirectoryPath;
            if (StringUtils.isNotBlank(templatesDirectory)) {
                templateDirectoryPath = templatesDirectory + "/hipchat";
            } else {
                templateDirectoryPath = System.getProperties().getProperty("user.dir") + "/src/main/resources/hipchat/templates";
            }

            final ChannelFreemarkerTemplatingService freemarkerTemplatingService = new ChannelFreemarkerTemplatingService(templateDirectoryPath);

            final HashMap<String, Object> model = new HashMap<>();
            model.put("projectDataCollection", projectDataCollection);

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

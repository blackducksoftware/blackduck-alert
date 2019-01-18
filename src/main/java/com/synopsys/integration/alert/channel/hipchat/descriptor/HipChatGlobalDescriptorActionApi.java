/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.channel.hipchat.descriptor;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;

@Component
public class HipChatGlobalDescriptorActionApi extends DescriptorActionApi {
    private final HipChatChannel hipChatChannel;
    private final ConfigurationFieldModelConverter modelConverter;

    @Autowired
    public HipChatGlobalDescriptorActionApi(final HipChatChannel hipChatChannel, final ConfigurationFieldModelConverter modelConverter) {
        this.hipChatChannel = hipChatChannel;
        this.modelConverter = modelConverter;
    }

    @Override
    public void testConfig(final Map<String, ConfigField> configFields, final TestConfigModel testConfig) throws IntegrationException {
        final FieldAccessor fieldAccessor = modelConverter.convertToFieldAccessor(configFields, testConfig.getFieldModel());
        final Optional<String> apiKey = fieldAccessor.getString(HipChatDescriptor.KEY_API_KEY);
        final String configuredApiUrl = fieldAccessor.getString(HipChatDescriptor.KEY_HOST_SERVER).orElse(HipChatChannel.HIP_CHAT_API);
        if (!apiKey.isPresent()) {
            throw new AlertException("ERROR: Missing global config.");
        }

        final RestConnection restConnection = hipChatChannel.getChannelRestConnectionFactory().createRestConnection();
        hipChatChannel.testApiKeyAndApiUrlConnection(restConnection, configuredApiUrl, apiKey.get());
        final Integer parsedRoomId;
        try {
            final String testRoomId = testConfig.getDestination().orElse(null);
            parsedRoomId = Integer.valueOf(testRoomId);
        } catch (final NumberFormatException e) {
            throw new AlertException("The provided room id is an invalid number.");
        }

        final String htmlMessage = "This is a test message sent by Alert.";
        final Request testRequest = hipChatChannel.createRequest(configuredApiUrl, apiKey.get(), parsedRoomId, Boolean.TRUE, "red", htmlMessage);
        hipChatChannel.sendMessageRequest(restConnection, testRequest, "test");
    }

}

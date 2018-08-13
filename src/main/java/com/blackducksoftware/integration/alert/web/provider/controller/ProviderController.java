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
package com.blackducksoftware.integration.alert.web.provider.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.DescriptorMap;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.enumeration.DescriptorConfigType;
import com.blackducksoftware.integration.alert.web.channel.actions.ChannelGlobalConfigActions;
import com.blackducksoftware.integration.alert.web.channel.controller.ConfigController;
import com.blackducksoftware.integration.alert.web.channel.handler.ChannelConfigHandler;
import com.blackducksoftware.integration.alert.web.model.Config;

@RestController
@RequestMapping(ConfigController.PROVIDER_CONFIG + "/{descriptorName}")
public class ProviderController extends ConfigController {
    private final DescriptorMap descriptorMap;
    private final ChannelConfigHandler channelConfigHandler;

    @Autowired
    public ProviderController(final DescriptorMap descriptorMap, final ContentConverter contentConverter, final ChannelGlobalConfigActions configActions) {
        this.descriptorMap = descriptorMap;
        channelConfigHandler = new ChannelConfigHandler(contentConverter, configActions);
    }

    @Override
    public List<? extends Config> getConfig(final Long id, final String descriptorName) {
        final DescriptorConfig providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getConfig(DescriptorConfigType.PROVIDER_CONFIG);
        return channelConfigHandler.getConfig(id, providerDescriptor);
    }

    @Override
    public ResponseEntity<String> postConfig(final String restModel, final String descriptorName) {
        final DescriptorConfig providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getConfig(DescriptorConfigType.PROVIDER_CONFIG);
        return channelConfigHandler.postConfig(providerDescriptor.getConfigFromJson(restModel), providerDescriptor);
    }

    @Override
    public ResponseEntity<String> putConfig(final String restModel, final String descriptorName) {
        final DescriptorConfig providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getConfig(DescriptorConfigType.PROVIDER_CONFIG);
        return channelConfigHandler.postConfig(providerDescriptor.getConfigFromJson(restModel), providerDescriptor);
    }

    @Override
    public ResponseEntity<String> validateConfig(final String restModel, final String descriptorName) {
        final DescriptorConfig providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getConfig(DescriptorConfigType.PROVIDER_CONFIG);
        return channelConfigHandler.postConfig(providerDescriptor.getConfigFromJson(restModel), providerDescriptor);
    }

    @Override
    public ResponseEntity<String> deleteConfig(final Long id, final String descriptorName) {
        final DescriptorConfig providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getConfig(DescriptorConfigType.PROVIDER_CONFIG);
        return channelConfigHandler.deleteConfig(id, providerDescriptor);
    }

    @Override
    public ResponseEntity<String> testConfig(final String restModel, final String descriptorName) {
        final DescriptorConfig providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getConfig(DescriptorConfigType.PROVIDER_CONFIG);
        return channelConfigHandler.postConfig(providerDescriptor.getConfigFromJson(restModel), providerDescriptor);
    }

}

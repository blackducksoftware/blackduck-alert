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
package com.blackducksoftware.integration.alert.web.channel.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.common.descriptor.DescriptorMap;
import com.blackducksoftware.integration.alert.common.descriptor.config.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfigType;
import com.blackducksoftware.integration.alert.web.channel.actions.ChannelGlobalConfigActions;
import com.blackducksoftware.integration.alert.web.channel.handler.ChannelConfigHandler;
import com.blackducksoftware.integration.alert.web.model.Config;

@RestController
@RequestMapping(ChannelConfigController.UNIVERSAL_PATH + "/global/{descriptorName}")
public class ChannelGlobalConfigController extends ChannelConfigController {
    private final ChannelConfigHandler<Config> controllerHandler;
    private final DescriptorMap descriptorMap;

    @Autowired
    public ChannelGlobalConfigController(final DescriptorMap descriptorMap, final ContentConverter contentConverter, final ChannelGlobalConfigActions channelGlobalConfigActions) {
        this.descriptorMap = descriptorMap;
        this.controllerHandler = new ChannelConfigHandler<>(contentConverter, channelGlobalConfigActions);
    }

    @Override
    public List<Config> getConfig(final Long id, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = descriptorMap.getChannelDescriptor(descriptorName);
        return controllerHandler.getConfig(id, descriptor);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = descriptorMap.getChannelDescriptor(descriptorName);
        final DatabaseContentConverter databaseContentConverter = descriptor.getConfig(DescriptorConfigType.GLOBAL_CONFIG).getDatabaseContentConverter();
        return controllerHandler.postConfig(databaseContentConverter.getRestModelFromJson(restModel), descriptor);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = descriptorMap.getChannelDescriptor(descriptorName);
        final DatabaseContentConverter databaseContentConverter = descriptor.getConfig(DescriptorConfigType.GLOBAL_CONFIG).getDatabaseContentConverter();
        return controllerHandler.putConfig(databaseContentConverter.getRestModelFromJson(restModel), descriptor);
    }

    @Override
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = descriptorMap.getChannelDescriptor(descriptorName);
        final DatabaseContentConverter databaseContentConverter = descriptor.getConfig(DescriptorConfigType.GLOBAL_CONFIG).getDatabaseContentConverter();
        return controllerHandler.validateConfig(databaseContentConverter.getRestModelFromJson(restModel), descriptor);
    }

    @Override
    public ResponseEntity<String> deleteConfig(final Long id, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = descriptorMap.getChannelDescriptor(descriptorName);
        return controllerHandler.deleteConfig(id, descriptor);
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = descriptorMap.getChannelDescriptor(descriptorName);
        final DatabaseContentConverter databaseContentConverter = descriptor.getConfig(DescriptorConfigType.GLOBAL_CONFIG).getDatabaseContentConverter();
        return controllerHandler.testConfig(databaseContentConverter.getRestModelFromJson(restModel), descriptor);
    }

}

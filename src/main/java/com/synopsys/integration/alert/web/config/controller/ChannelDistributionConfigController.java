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
package com.synopsys.integration.alert.web.config.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.enumeration.DescriptorConfigType;
import com.synopsys.integration.alert.web.channel.actions.ChannelDistributionConfigActions;
import com.synopsys.integration.alert.web.config.controller.handler.ConfigControllerHandler;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

@RestController
@RequestMapping(ConfigController.CHANNEL_CONFIG + "/distribution")
public class ChannelDistributionConfigController extends ConfigController {
    private final ConfigControllerHandler controllerHandler;
    private final DescriptorMap descriptorMap;

    @Autowired
    public ChannelDistributionConfigController(final DescriptorMap descriptorMap, final ContentConverter contentConverter, final ChannelDistributionConfigActions channelDistributionConfigActions) {
        this.descriptorMap = descriptorMap;
        this.controllerHandler = new ConfigControllerHandler(contentConverter, channelDistributionConfigActions);
    }

    @GetMapping()
    public List<? extends Config> getConfig() {
        final List<Config> configs = new ArrayList<>();
        final Set<String> descriptorNames = descriptorMap.getChannelDescriptorMap().keySet();
        for (final String descriptorName : descriptorNames) {
            configs.addAll(getConfig(null, descriptorName));
        }
        return configs;
    }

    @Override
    @GetMapping("/{descriptorName}")
    public List<? extends Config> getConfig(final Long id, @PathVariable final String descriptorName) {
        final DescriptorConfig descriptor = descriptorMap.getChannelDescriptor(descriptorName).getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        return controllerHandler.getConfig(id, descriptor);
    }

    @Override
    @PostMapping("/{descriptorName}")
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final DescriptorConfig descriptor = descriptorMap.getChannelDescriptor(descriptorName).getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        final CommonDistributionConfig parsedRestModel = (CommonDistributionConfig) descriptor.getConfigFromJson(restModel);
        return controllerHandler.postConfig(parsedRestModel, descriptor);
    }

    @Override
    @PutMapping("/{descriptorName}")
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final DescriptorConfig descriptor = descriptorMap.getChannelDescriptor(descriptorName).getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        final CommonDistributionConfig parsedRestModel = (CommonDistributionConfig) descriptor.getConfigFromJson(restModel);
        return controllerHandler.putConfig(parsedRestModel, descriptor);
    }

    @Override
    @PostMapping("/{descriptorName}/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final DescriptorConfig descriptor = descriptorMap.getChannelDescriptor(descriptorName).getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        final CommonDistributionConfig parsedRestModel = (CommonDistributionConfig) descriptor.getConfigFromJson(restModel);
        return controllerHandler.validateConfig(parsedRestModel, descriptor);
    }

    @Override
    @DeleteMapping("/{descriptorName}")
    public ResponseEntity<String> deleteConfig(final Long id, @PathVariable final String descriptorName) {
        final DescriptorConfig descriptor = descriptorMap.getChannelDescriptor(descriptorName).getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        return controllerHandler.deleteConfig(id, descriptor);
    }

    @Override
    @PostMapping("/{descriptorName}/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final DescriptorConfig descriptor = descriptorMap.getChannelDescriptor(descriptorName).getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        final CommonDistributionConfig parsedRestModel = (CommonDistributionConfig) descriptor.getConfigFromJson(restModel);
        return controllerHandler.testConfig(parsedRestModel, descriptor);
    }

}

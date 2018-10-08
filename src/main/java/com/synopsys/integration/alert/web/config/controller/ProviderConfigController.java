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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.enumeration.DescriptorActionApi;
import com.synopsys.integration.alert.web.config.actions.SingleEntityConfigActions;
import com.synopsys.integration.alert.web.config.controller.handler.ConfigControllerHandler;
import com.synopsys.integration.alert.web.model.Config;

@RestController
@RequestMapping(ConfigController.PROVIDER_CONFIG + "/{descriptorName}")
public class ProviderConfigController extends ConfigController {
    private final DescriptorMap descriptorMap;
    private final ConfigControllerHandler configControllerHandler;

    @Autowired
    public ProviderConfigController(final DescriptorMap descriptorMap, final ContentConverter contentConverter, final SingleEntityConfigActions configActions) {
        this.descriptorMap = descriptorMap;
        configControllerHandler = new ConfigControllerHandler(contentConverter, configActions);
    }

    @Override
    public List<? extends Config> getConfig(final Long id, @PathVariable final String descriptorName) {
        final RestApi providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getRestApi(DescriptorActionApi.PROVIDER_CONFIG);
        return configControllerHandler.getConfig(id, providerDescriptor);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final RestApi providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getRestApi(DescriptorActionApi.PROVIDER_CONFIG);
        return configControllerHandler.postConfig(providerDescriptor.getConfigFromJson(restModel), providerDescriptor);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final RestApi providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getRestApi(DescriptorActionApi.PROVIDER_CONFIG);
        return configControllerHandler.putConfig(providerDescriptor.getConfigFromJson(restModel), providerDescriptor);
    }

    @Override
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final RestApi providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getRestApi(DescriptorActionApi.PROVIDER_CONFIG);
        return configControllerHandler.validateConfig(providerDescriptor.getConfigFromJson(restModel), providerDescriptor);
    }

    @Override
    public ResponseEntity<String> deleteConfig(final Long id, @PathVariable final String descriptorName) {
        final RestApi providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getRestApi(DescriptorActionApi.PROVIDER_CONFIG);
        return configControllerHandler.deleteConfig(id, providerDescriptor);
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final String restModel, @PathVariable final String descriptorName) {
        final RestApi providerDescriptor = descriptorMap.getProviderDescriptor(descriptorName).getRestApi(DescriptorActionApi.PROVIDER_CONFIG);
        return configControllerHandler.testConfig(providerDescriptor.getConfigFromJson(restModel), providerDescriptor);
    }

}

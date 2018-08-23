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
import com.synopsys.integration.alert.common.enumeration.RestApiTypes;
import com.synopsys.integration.alert.web.config.actions.SingleEntityConfigActions;
import com.synopsys.integration.alert.web.config.controller.handler.ConfigControllerHandler;
import com.synopsys.integration.alert.web.model.Config;

@RestController
@RequestMapping(ConfigController.COMPONENT_CONFIG + "/{descriptorName}")
public class ComponentConfigController extends ConfigController {
    private final DescriptorMap descriptorMap;
    private final ConfigControllerHandler controllerHandler;

    @Autowired
    public ComponentConfigController(final DescriptorMap descriptorMap, final ContentConverter contentConverter, final SingleEntityConfigActions singleEntityConfigActions) {
        this.descriptorMap = descriptorMap;
        this.controllerHandler = new ConfigControllerHandler(contentConverter, singleEntityConfigActions);
    }

    @Override
    public List<? extends Config> getConfig(final Long id, @PathVariable final String descriptorName) {
        final RestApi descriptor = descriptorMap.getComponentDescriptor(descriptorName).getRestApi(RestApiTypes.COMPONENT_CONFIG);
        return controllerHandler.getConfig(id, descriptor);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final String config, @PathVariable final String descriptorName) {
        final RestApi descriptor = descriptorMap.getComponentDescriptor(descriptorName).getRestApi(RestApiTypes.COMPONENT_CONFIG);
        return controllerHandler.postConfig(descriptor.getConfigFromJson(config), descriptor);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final String config, @PathVariable final String descriptorName) {
        final RestApi descriptor = descriptorMap.getComponentDescriptor(descriptorName).getRestApi(RestApiTypes.COMPONENT_CONFIG);
        return controllerHandler.putConfig(descriptor.getConfigFromJson(config), descriptor);
    }

    @Override
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final String config, @PathVariable final String descriptorName) {
        final RestApi descriptor = descriptorMap.getComponentDescriptor(descriptorName).getRestApi(RestApiTypes.COMPONENT_CONFIG);
        return controllerHandler.validateConfig(descriptor.getConfigFromJson(config), descriptor);
    }

    @Override
    public ResponseEntity<String> deleteConfig(final Long id, final @PathVariable String descriptorName) {
        final RestApi descriptor = descriptorMap.getComponentDescriptor(descriptorName).getRestApi(RestApiTypes.COMPONENT_CONFIG);
        return controllerHandler.deleteConfig(id, descriptor);
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final String config, final @PathVariable String descriptorName) {
        return controllerHandler.doNotAllowHttpMethod();
    }

}

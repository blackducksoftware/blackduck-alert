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
package com.blackducksoftware.integration.hub.alert.web.test.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.google.gson.Gson;

@RestController
@RequestMapping(UniversalConfigController.UNIVERSAL_PATH + "/distribution")
public class UniversalDistributionConfigController extends UniversalConfigController<ChannelDescriptor> {
    private final UniversalDistributionConfigHandler controllerHandler;
    private final Gson gson;

    @Autowired
    public UniversalDistributionConfigController(final Gson gson, final List<ChannelDescriptor> descriptors, final ObjectTransformer objectTransformer, final UniversalDistributionConfigHandler controllerHandler) {
        super(descriptors);
        this.gson = gson;
        this.controllerHandler = controllerHandler;
    }

    @Override
    public List<CommonDistributionConfigRestModel> getConfig(final Long id, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = getDescriptor(descriptorName);
        return controllerHandler.getConfig(id, descriptor);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = getDescriptor(descriptorName);
        return controllerHandler.postConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = getDescriptor(descriptorName);
        return controllerHandler.putConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    @Override
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = getDescriptor(descriptorName);
        return controllerHandler.validateConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    @Override
    public ResponseEntity<String> deleteConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = getDescriptor(descriptorName);
        return controllerHandler.deleteConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = true) final String restModel, @PathVariable final String descriptorName) {
        final ChannelDescriptor descriptor = getDescriptor(descriptorName);
        return controllerHandler.testConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

}

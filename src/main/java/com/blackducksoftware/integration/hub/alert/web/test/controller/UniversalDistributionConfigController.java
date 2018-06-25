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
package com.blackducksoftware.integration.hub.alert.web.test.controller;

import java.util.List;

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

import com.blackducksoftware.integration.hub.alert.channel.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.controller.DistributionConfigController;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.google.gson.Gson;

@RestController
@RequestMapping(DistributionConfigController.DISTRIBUTION_PATH + "/universal/{channelName}")
public class UniversalDistributionConfigController {
    private final UniversalDistributionConfigHandler universalDistributionConfigHandler;
    private final List<ChannelDescriptor> descriptors;
    private final Gson gson;

    @Autowired
    public UniversalDistributionConfigController(final UniversalDistributionConfigHandler universalDistributionConfigHandler, final List<ChannelDescriptor> descriptors, final Gson gson) {
        this.universalDistributionConfigHandler = universalDistributionConfigHandler;
        this.descriptors = descriptors;
        this.gson = gson;
    }

    @GetMapping
    public List<CommonDistributionConfigRestModel> getConfig(final Long id, @PathVariable final String channelName) throws AlertException {
        final ChannelDescriptor descriptor = getDescriptor(channelName);
        return universalDistributionConfigHandler.getConfig(id, descriptor);
    }

    @PostMapping
    public ResponseEntity<String> postConfig(@RequestBody(required = true) final String restModel, @PathVariable final String channelName) {
        final ChannelDescriptor descriptor = getDescriptor(channelName);
        return universalDistributionConfigHandler.postConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    @PutMapping
    public ResponseEntity<String> putConfig(@RequestBody(required = true) final String restModel, @PathVariable final String channelName) {
        final ChannelDescriptor descriptor = getDescriptor(channelName);
        return universalDistributionConfigHandler.putConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    @PostMapping(value = "/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) final String restModel, @PathVariable final String channelName) {
        final ChannelDescriptor descriptor = getDescriptor(channelName);
        return universalDistributionConfigHandler.validateConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteConfig(@RequestBody(required = true) final String restModel, @PathVariable final String channelName) {
        final ChannelDescriptor descriptor = getDescriptor(channelName);
        return universalDistributionConfigHandler.deleteConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    @PostMapping(value = "/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = true) final String restModel, @PathVariable final String channelName) {
        final ChannelDescriptor descriptor = getDescriptor(channelName);
        return universalDistributionConfigHandler.testConfig(gson.fromJson(restModel, descriptor.getDistributionRestModelClass()), descriptor);
    }

    private ChannelDescriptor getDescriptor(final String descriptorName) {
        for (final ChannelDescriptor descriptor : descriptors) {
            if (descriptorName.equals(descriptor.getName())) {
                return descriptor;
            }
        }

        return null;
    }
}

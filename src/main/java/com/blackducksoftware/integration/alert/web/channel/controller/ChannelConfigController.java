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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.blackducksoftware.integration.alert.web.controller.BaseController;
import com.blackducksoftware.integration.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.alert.web.model.Config;

@RequestMapping(ChannelConfigController.UNIVERSAL_PATH)
public abstract class ChannelConfigController extends BaseController {
    public static final String UNIVERSAL_PATH = ConfigController.CONFIGURATION_PATH + "/channel";

    @GetMapping
    public abstract List<Config> getConfig(final Long id, final String descriptorName);

    @PostMapping
    public abstract ResponseEntity<String> postConfig(String restModel, final String descriptorName);

    @PutMapping
    public abstract ResponseEntity<String> putConfig(String restModel, final String descriptorName);

    @PostMapping(value = "/validate")
    public abstract ResponseEntity<String> validateConfig(String restModel, final String descriptorName);

    @DeleteMapping
    public abstract ResponseEntity<String> deleteConfig(String restModel, final String descriptorName);

    @PostMapping(value = "/test")
    public abstract ResponseEntity<String> testConfig(String restModel, final String descriptorName);
}

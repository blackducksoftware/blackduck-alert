/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.HipChatConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

@RestController
public class HipChatConfigController implements ConfigController<HipChatConfigEntity, HipChatConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(HipChatConfigController.class);
    private final HipChatConfigActions configActions;
    private final CommonConfigController<HipChatConfigEntity, HipChatConfigRestModel> commonConfigController;

    @Autowired
    public HipChatConfigController(final HipChatConfigActions configActions) {
        this.configActions = configActions;
        commonConfigController = new CommonConfigController<>(HipChatConfigEntity.class, HipChatConfigRestModel.class, configActions);
    }

    @Override
    @GetMapping(value = "/configuration/hipchat")
    public List<HipChatConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return commonConfigController.getConfig(id);
    }

    @Override
    @PostMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final HipChatConfigRestModel hipChatConfig) {
        return commonConfigController.postConfig(hipChatConfig);
    }

    @Override
    @PutMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final HipChatConfigRestModel hipChatConfig) {
        return commonConfigController.putConfig(hipChatConfig);
    }

    @Override
    public ResponseEntity<String> validateConfig(final HipChatConfigRestModel hipChatConfig) {
        return commonConfigController.validateConfig(hipChatConfig);
    }

    @Override
    @DeleteMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final HipChatConfigRestModel hipChatConfig) {
        return commonConfigController.deleteConfig(hipChatConfig);
    }

    @Override
    @PostMapping(value = "/configuration/hipchat/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final HipChatConfigRestModel hipChatConfig) {
        return commonConfigController.testConfig(hipChatConfig);
    }

}

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
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.SlackConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.SlackConfigRestModel;
import com.google.gson.Gson;

@RestController
public class SlackConfigController implements ConfigController<SlackConfigEntity, SlackConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(HipChatConfigController.class);
    private final SlackConfigActions configActions;
    private final Gson gson;
    private final CommonConfigController<SlackConfigEntity, SlackConfigRestModel> commonConfigController;

    @Autowired
    public SlackConfigController(final SlackConfigActions configActions, final Gson gson) {
        this.configActions = configActions;
        this.gson = gson;
        commonConfigController = new CommonConfigController<>(SlackConfigEntity.class, SlackConfigRestModel.class, configActions);
    }

    @Override
    @GetMapping(value = "/configuration/slack")
    public List<SlackConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return commonConfigController.getConfig(id);
    }

    @Override
    @PostMapping(value = "/configuration/slack")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "slackConfig", required = true) @RequestBody final SlackConfigRestModel slackConfig) {
        return commonConfigController.postConfig(slackConfig);
    }

    @Override
    @PutMapping(value = "/configuration/slack")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "slackConfig", required = true) @RequestBody final SlackConfigRestModel slackConfig) {
        return commonConfigController.putConfig(slackConfig);
    }

    @Override
    @DeleteMapping(value = "/configuration/slack")
    public ResponseEntity<String> deleteConfig(@RequestAttribute(value = "slackConfig", required = true) @RequestBody final SlackConfigRestModel slackConfig) {
        return commonConfigController.deleteConfig(slackConfig);
    }

    @Override
    @PostMapping(value = "/configuration/slack/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final SlackConfigRestModel slackConfig) {
        return commonConfigController.testConfig(slackConfig);
    }

    @Override
    public ResponseEntity<String> validateConfig(final SlackConfigRestModel restModel) {
        return commonConfigController.validateConfig(restModel);
    }

}

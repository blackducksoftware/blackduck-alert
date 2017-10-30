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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.web.actions.HipChatConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

@RestController
public class HipChatConfigController implements ConfigController<HipChatConfigEntity, HipChatConfigRestModel> {
    private final Logger logger = LoggerFactory.getLogger(HipChatConfigController.class);
    private final HipChatConfigActions configActions;
    private final CommonConfigController<HipChatConfigEntity, HipChatConfigRestModel> commonConfigController;

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
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "hipChatConfig", required = true) @RequestBody final HipChatConfigRestModel hipChatConfig) {
        return commonConfigController.postConfig(hipChatConfig);
    }

    @Override
    @PutMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "hipChatConfig", required = true) @RequestBody final HipChatConfigRestModel hipChatConfig) {
        return commonConfigController.putConfig(hipChatConfig);
    }

    @Override
    public ResponseEntity<String> validateConfig(final HipChatConfigRestModel hipChatConfig) {
        // TODO
        return null;
    }

    @Override
    @DeleteMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> deleteConfig(@RequestAttribute(value = "hipChatConfig", required = true) @RequestBody final HipChatConfigRestModel hipChatConfig) {
        return commonConfigController.deleteConfig(hipChatConfig);
    }

    @Override
    @PostMapping(value = "/configuration/hipchat/test")
    public ResponseEntity<String> testConfig(@RequestAttribute(value = "hipChatConfig", required = true) @RequestBody final HipChatConfigRestModel hipChatConfig) {
        final HipChatChannel channel = new HipChatChannel(null, (HipChatRepository) configActions.repository);
        String responseMessage = null;
        try {
            responseMessage = channel.testMessage(configActions.objectTransformer.transformObject(hipChatConfig, HipChatConfigEntity.class));
        } catch (final IntegrationException e) {
            logger.error(e.getMessage(), e);
            return commonConfigController.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, hipChatConfig.getId(), e.getMessage());
        }
        final Long id = configActions.objectTransformer.stringToLong(hipChatConfig.getId());
        try {
            final int intResponse = Integer.parseInt(responseMessage);
            final HttpStatus status = HttpStatus.valueOf(intResponse);
            if (status != null) {
                return commonConfigController.createResponse(status, id, "Attempting to send a test message.");
            }
        } catch (final IllegalArgumentException e) {
            return commonConfigController.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, id, e.getMessage());
        }
        return commonConfigController.createResponse(HttpStatus.BAD_REQUEST, id, "Failure.");
    }

}

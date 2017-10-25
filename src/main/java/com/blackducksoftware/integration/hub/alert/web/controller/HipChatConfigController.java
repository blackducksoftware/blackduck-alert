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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

@RestController
public class HipChatConfigController extends ConfigController<HipChatConfigEntity, HipChatConfigRestModel> {

    @Autowired
    public HipChatConfigController(final HipChatRepository repository) {
        super(repository);
    }

    @Override
    @GetMapping(value = "/configuration/hipchat")
    public List<HipChatConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return super.getConfig(id);
    }

    @Override
    @PostMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "hipChatConfig", required = true) @RequestBody final HipChatConfigRestModel hipChatConfig) {
        return super.postConfig(hipChatConfig);
    }

    @Override
    @PutMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "hipChatConfig", required = true) @RequestBody final HipChatConfigRestModel hipChatConfig) {
        return super.putConfig(hipChatConfig);
    }

    @Override
    @DeleteMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> deleteConfig(@RequestAttribute(value = "hipChatConfig", required = true) @RequestBody final HipChatConfigRestModel hipChatConfig) {
        return super.deleteConfig(hipChatConfig);
    }

    @Override
    @PostMapping(value = "/configuration/hipchat/test")
    public ResponseEntity<String> testConfig(@RequestAttribute(value = "hipChatConfig", required = true) @RequestBody final HipChatConfigRestModel hipChatConfig) {
        final HipChatChannel channel = new HipChatChannel(null, (HipChatRepository) repository);
        final String responseMessage = channel.testMessage(restModelToDatabaseModel(hipChatConfig));
        try {
            final int intResponse = Integer.parseInt(responseMessage);
            final HttpStatus status = HttpStatus.valueOf(intResponse);
            if (status != null) {
                return super.createResponse(status, hipChatConfig.getId(), "Attempting to send test message.");
            }
        } catch (final IllegalArgumentException e) {
            return super.createResponse(HttpStatus.INTERNAL_SERVER_ERROR, hipChatConfig.getId(), e.getMessage());
        }
        return super.createResponse(HttpStatus.BAD_REQUEST, hipChatConfig.getId(), "Failure.");
    }

    @Override
    public HipChatConfigEntity restModelToDatabaseModel(final HipChatConfigRestModel model) {
        return new HipChatConfigEntity(model.getId(), model.getApiKey(), model.getRoomId(), model.getNotify(), model.getColor());
    }

    @Override
    public HipChatConfigRestModel databaseModelToRestModel(final HipChatConfigEntity entity) {
        return new HipChatConfigRestModel(entity.getId(), entity.getApiKey(), entity.getRoomId(), entity.getNotify(), entity.getColor());
    }

    @Override
    public List<HipChatConfigRestModel> databaseModelsToRestModels(final List<HipChatConfigEntity> databaseModels) {
        final List<HipChatConfigRestModel> restModels = new ArrayList<>();
        for (final HipChatConfigEntity databaseModel : databaseModels) {
            restModels.add(new HipChatConfigRestModel(databaseModel.getId(), databaseModel.getApiKey(), databaseModel.getRoomId(), databaseModel.getNotify(), databaseModel.getColor()));
        }
        return restModels;
    }
}

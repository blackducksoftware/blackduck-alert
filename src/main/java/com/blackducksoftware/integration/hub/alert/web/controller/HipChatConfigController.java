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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

@RestController
public class HipChatConfigController implements ChannelController<HipChatConfigEntity, HipChatConfigRestModel> {
    private final HipChatRepository hipChatRepository;

    @Autowired
    HipChatConfigController(final HipChatRepository hipChatRepository) {
        this.hipChatRepository = hipChatRepository;
    }

    @Override
    @GetMapping(value = "/configuration/hipchat")
    public List<HipChatConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        if (id != null) {
            final HipChatConfigEntity foundEntity = hipChatRepository.findOne(id);
            if (foundEntity != null) {
                return Arrays.asList(databaseModelToRestModel(foundEntity));
            } else {
                return Collections.emptyList();
            }
        }
        return databaseModelsToRestModels(hipChatRepository.findAll());
    }

    @Override
    @PostMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "hipChatModel", required = true) @RequestBody final HipChatConfigRestModel hipChatModel) {
        final HipChatConfigEntity hipChatEntity = restModelToDatabaseModel(hipChatModel);
        if (hipChatEntity.getId() == null || !hipChatRepository.exists(hipChatEntity.getId())) {
            URI uri;
            try {
                uri = new URI("/configuration/hipchat");
            } catch (final URISyntaxException e) {
                return ResponseEntity.status(500).body(e.getMessage());
            }
            final HipChatConfigEntity createdEntity = hipChatRepository.save(hipChatEntity);
            return ResponseEntity.created(uri).body("\"id\" : " + createdEntity.getId());
        }
        return ResponseEntity.status(409).body("Invalid id");
    }

    @Override
    @PutMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "hipChatModel", required = true) @RequestBody final HipChatConfigRestModel hipChatModel) {
        final HipChatConfigEntity hipChatEntity = restModelToDatabaseModel(hipChatModel);
        if (hipChatEntity.getId() != null && hipChatRepository.exists(hipChatEntity.getId())) {
            URI uri;
            try {
                uri = new URI("/configuration/hipchat");
            } catch (final URISyntaxException e) {
                return ResponseEntity.status(500).body("error: " + e.getMessage());
            }
            hipChatRepository.save(hipChatEntity);
            return ResponseEntity.created(uri).build();
        }
        return ResponseEntity.badRequest().body("No configuration with id " + hipChatEntity.getId());
    }

    @Override
    @DeleteMapping(value = "/configuration/hipchat")
    public ResponseEntity<String> deleteConfig(@RequestAttribute(value = "hipChatModel", required = true) @RequestBody final HipChatConfigRestModel hipChatModel) {
        if (hipChatModel.getId() != null && hipChatRepository.exists(hipChatModel.getId())) {
            hipChatRepository.delete(hipChatModel.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("No configuration with id " + hipChatModel.getId());
    }

    @Override
    @PostMapping(value = "/configuration/hipchat/test")
    public ResponseEntity<String> testConfig(@RequestAttribute(value = "hipChatModel", required = true) @RequestBody final HipChatConfigRestModel hipChatModel) {
        // TODO implement method for testing the configuration
        return ResponseEntity.notFound().build();
    }

    @Override
    public HipChatConfigEntity restModelToDatabaseModel(final HipChatConfigRestModel model) {
        return new HipChatConfigEntity(model.getApiKey(), model.getRoomId(), model.getNotify(), model.getColor());
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

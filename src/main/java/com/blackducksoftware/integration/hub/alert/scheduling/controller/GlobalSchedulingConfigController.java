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
package com.blackducksoftware.integration.hub.alert.scheduling.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.scheduling.repository.global.GlobalSchedulingConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;

@RestController
public class GlobalSchedulingConfigController extends ConfigController<GlobalSchedulingConfigRestModel> {
    private final GlobalSchedulingHandler globalSchedulingHandler;

    @Autowired
    GlobalSchedulingConfigController(final GlobalSchedulingConfigActions configActions, final ObjectTransformer objectTransformer) {
        globalSchedulingHandler = new GlobalSchedulingHandler(GlobalSchedulingConfigEntity.class, GlobalSchedulingConfigRestModel.class, configActions, objectTransformer);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/configuration/global/scheduling")
    public List<GlobalSchedulingConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return globalSchedulingHandler.getConfig(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/global/scheduling")
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final GlobalSchedulingConfigRestModel restModel) {
        return globalSchedulingHandler.postConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/configuration/global/scheduling")
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final GlobalSchedulingConfigRestModel restModel) {
        return globalSchedulingHandler.putConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/global/scheduling/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final GlobalSchedulingConfigRestModel restModel) {
        return globalSchedulingHandler.validateConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/configuration/global/scheduling")
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final GlobalSchedulingConfigRestModel restModel) {
        return globalSchedulingHandler.deleteConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/global/scheduling/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final GlobalSchedulingConfigRestModel restModel) {
        return globalSchedulingHandler.doNotAllowHttpMethod();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/global/accumulator/run")
    public ResponseEntity<String> runAccumulator(@RequestBody(required = false) final GlobalSchedulingConfigRestModel restModel) {
        return globalSchedulingHandler.runAccumulator();
    }

}

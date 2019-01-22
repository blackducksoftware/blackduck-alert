/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;

@RestController
@RequestMapping(ConfigController.CONFIGURATION_PATH)
public class ConfigController extends BaseController {
    public static final String CONFIGURATION_PATH = BaseController.BASE_PATH + "/configuration";

    private final ConfigControllerHandler controllerHandler;

    @Autowired
    public ConfigController(final ConfigControllerHandler controllerHandler) {
        this.controllerHandler = controllerHandler;
    }

    @GetMapping
    public ResponseEntity<String> getConfigs(final @RequestParam ConfigContextEnum context, @RequestParam(required = false) final String descriptorName) {
        return controllerHandler.getConfigs(context, descriptorName);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getConfig(@PathVariable final Long id) {
        return controllerHandler.getConfig(id);
    }

    @PostMapping
    public ResponseEntity<String> postConfig(@RequestBody(required = true) final FieldModel restModel) {
        return controllerHandler.postConfig(restModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> putConfig(@PathVariable final Long id, @RequestBody(required = true) final FieldModel restModel) {
        return controllerHandler.putConfig(id, restModel);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) final FieldModel restModel) {
        return controllerHandler.validateConfig(restModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConfig(@PathVariable final Long id) {
        return controllerHandler.deleteConfig(id);
    }

    @PostMapping("/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = true) final FieldModel restModel, @RequestParam(required = false) final String destination) {
        return controllerHandler.testConfig(restModel, destination);
    }
}

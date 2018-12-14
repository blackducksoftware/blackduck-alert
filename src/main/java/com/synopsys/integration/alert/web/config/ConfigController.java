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
package com.synopsys.integration.alert.web.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.web.controller.BaseController;
import com.synopsys.integration.alert.web.model.FieldModel;

@RequestMapping(ConfigController.CONFIGURATION_PATH)
public class ConfigController extends BaseController {
    public static final String CONFIGURATION_PATH = BaseController.BASE_PATH + "/configuration";

    private final ConfigControllerHandler controllerHandler;

    // TODO Change the endpoint to have configuration/context/type/descriptorName/id

    @Autowired
    public ConfigController(final ConfigControllerHandler controllerHandler) {
        this.controllerHandler = controllerHandler;
    }

    public List<FieldModel> getConfigs(final @RequestParam ConfigContextEnum context, @RequestParam(required = false) final String descriptorName) {
        return controllerHandler.getConfigs(context, descriptorName);
    }

    public FieldModel getConfig(final Long id) {
        return controllerHandler.getConfig(id);
    }

    public ResponseEntity<String> postConfig(@RequestBody(required = false) final FieldModel restModel, @RequestParam final ConfigContextEnum context) {
        return controllerHandler.postConfig(restModel, context);
    }

    public ResponseEntity<String> putConfig(@RequestBody(required = false) final FieldModel restModel) {
        return controllerHandler.putConfig(restModel);
    }

    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final FieldModel restModel) {
        return controllerHandler.validateConfig(restModel);
    }

    public ResponseEntity<String> deleteConfig(final Long id) {
        return controllerHandler.deleteConfig(id);
    }

    public ResponseEntity<String> testConfig(@RequestBody(required = false) final FieldModel restModel, @RequestParam(required = false) final String destination) {
        return controllerHandler.testConfig(restModel);
    }
}

/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.web.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.ConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.TestController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.web.common.BaseController;

@RestController
@RequestMapping(ConfigController.CONFIGURATION_PATH)
public class ConfigController implements ConfigResourceController, TestController<FieldModel>, ValidateController<FieldModel> {
    public static final String CONFIGURATION_PATH = BaseController.BASE_PATH + "/configuration";
    private final ConfigActions configActions;

    @Autowired
    public ConfigController(ConfigActions configActions) {
        this.configActions = configActions;
    }

    @GetMapping
    public List<FieldModel> getAll(@RequestParam ConfigContextEnum context, @RequestParam(required = false) String descriptorName) {
        return ResponseFactory.createContentResponseFromAction(configActions.readAllByContextAndDescriptorAfterChecks(context.name(), descriptorName));
    }

    @Override
    public FieldModel getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(configActions.getOne(id));
    }

    @Override
    public FieldModel create(FieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.create(resource));
    }

    @Override
    public void update(Long id, FieldModel resource) {
        ResponseFactory.createContentResponseFromAction(configActions.updateAfterChecks(id, resource));
    }

    @Override
    public ValidationResponseModel validate(FieldModel requestBody) {
        ValidationActionResponse response = configActions.validate(requestBody);
        // TODO A better API for validation. Validation response should not be null.
        return ResponseFactory.createContentResponseFromAction(new ValidationActionResponse(HttpStatus.OK, response.getContent().orElse(null)));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createContentResponseFromAction(configActions.delete(id));
    }

    @Override
    public ValidationResponseModel test(FieldModel resource) {
        return ResponseFactory.createContentResponseFromAction(configActions.test(resource));
    }
}

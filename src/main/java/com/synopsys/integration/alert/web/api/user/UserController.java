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
package com.synopsys.integration.alert.web.api.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.BaseResourceController;
import com.synopsys.integration.alert.common.rest.api.ReadAllController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.web.api.config.ConfigController;

@RestController
@RequestMapping(UserController.USER_BASE_PATH)
public class UserController implements ReadAllController<UserConfig>, BaseResourceController<UserConfig>, ValidateController<UserConfig> {
    public static final String USER_BASE_PATH = ConfigController.CONFIGURATION_PATH + "/user";

    private final UserActions actions;

    @Autowired
    public UserController(UserActions actions) {
        this.actions = actions;
    }

    @Override
    public UserConfig create(UserConfig resource) {
        return ResponseFactory.createContentResponseFromAction(actions.create(resource));
    }

    @Override
    public UserConfig getOne(Long id) {
        return ResponseFactory.createContentResponseFromAction(actions.getOne(id));
    }

    @Override
    public void update(Long id, UserConfig resource) {
        ResponseFactory.createResponseFromAction(actions.update(id, resource));
    }

    @Override
    public void delete(Long id) {
        ResponseFactory.createResponseFromAction(actions.delete(id));
    }

    @Override
    public List<UserConfig> getAll() {
        return ResponseFactory.createContentResponseFromAction(actions.getAll());
    }

    @Override
    public ValidationResponseModel validate(UserConfig requestBody) {
        return ResponseFactory.createContentResponseFromAction(actions.validate(requestBody));
    }
}

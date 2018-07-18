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
package com.blackducksoftware.integration.alert.web.provider.hub;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.provider.hub.model.GlobalHubRepository;
import com.blackducksoftware.integration.alert.web.controller.GlobalConfigController;
import com.blackducksoftware.integration.alert.web.controller.handler.CommonConfigHandler;
import com.blackducksoftware.integration.alert.web.controller.handler.CommonGlobalConfigHandler;

@RestController
@RequestMapping(GlobalConfigController.PROVIDER_PATH + "/hub")
public class GlobalHubConfigController extends GlobalConfigController<GlobalHubConfigRestModel> {
    private final CommonConfigHandler<GlobalHubConfigEntity, GlobalHubConfigRestModel, GlobalHubRepository> commonConfigHandler;

    @Autowired
    public GlobalHubConfigController(final GlobalHubConfigActions configActions, final ObjectTransformer objectTransformer) {
        commonConfigHandler = new CommonGlobalConfigHandler<>(GlobalHubConfigEntity.class, GlobalHubConfigRestModel.class, configActions, objectTransformer);
    }

    @Override
    public List<GlobalHubConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return commonConfigHandler.getConfig(id);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final GlobalHubConfigRestModel globalConfig) {
        return commonConfigHandler.postConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final GlobalHubConfigRestModel globalConfig) {
        return commonConfigHandler.putConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> validateConfig(final GlobalHubConfigRestModel globalConfig) {
        return commonConfigHandler.validateConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final GlobalHubConfigRestModel globalConfig) {
        return commonConfigHandler.deleteConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final GlobalHubConfigRestModel globalConfig) {
        return commonConfigHandler.testConfig(globalConfig);
    }
}

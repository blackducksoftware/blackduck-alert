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
package com.blackducksoftware.integration.alert.web.provider.blackduck;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.alert.web.controller.GlobalConfigController;
import com.blackducksoftware.integration.alert.web.controller.handler.CommonConfigHandler;
import com.blackducksoftware.integration.alert.web.controller.handler.CommonGlobalConfigHandler;

@RestController
@RequestMapping(GlobalConfigController.PROVIDER_PATH + "/blackduck")
public class GlobalBlackDuckConfigController extends GlobalConfigController<GlobalBlackDuckConfig> {
    private final CommonConfigHandler<GlobalBlackDuckConfigEntity, GlobalBlackDuckConfig, GlobalBlackDuckRepository> commonConfigHandler;

    @Autowired
    public GlobalBlackDuckConfigController(final GlobalBlackDuckConfigActions configActions, final ContentConverter contentConverter) {
        commonConfigHandler = new CommonGlobalConfigHandler<>(GlobalBlackDuckConfigEntity.class, GlobalBlackDuckConfig.class, configActions, contentConverter);
    }

    @Override
    public List<GlobalBlackDuckConfig> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return commonConfigHandler.getConfig(id);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final GlobalBlackDuckConfig globalConfig) {
        return commonConfigHandler.postConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final GlobalBlackDuckConfig globalConfig) {
        return commonConfigHandler.putConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> validateConfig(final GlobalBlackDuckConfig globalConfig) {
        return commonConfigHandler.validateConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final GlobalBlackDuckConfig globalConfig) {
        return commonConfigHandler.deleteConfig(globalConfig);
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final GlobalBlackDuckConfig globalConfig) {
        return commonConfigHandler.testConfig(globalConfig);
    }
}

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
package com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.global;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.GlobalConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.CommonConfigHandler;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.CommonGlobalConfigHandler;

@RestController
@RequestMapping(GlobalConfigController.CHANNEL_PATH + "/hipchat")
public class GlobalHipChatConfigController extends GlobalConfigController<GlobalHipChatConfigRestModel> {
    private final CommonConfigHandler<GlobalHipChatConfigEntity, GlobalHipChatConfigRestModel, GlobalHipChatRepository> commonConfigHandler;

    @Autowired
    public GlobalHipChatConfigController(final GlobalHipChatConfigActions configActions, final ObjectTransformer objectTransformer) {
        commonConfigHandler = new CommonGlobalConfigHandler<>(GlobalHipChatConfigEntity.class, GlobalHipChatConfigRestModel.class, configActions, objectTransformer);
    }

    @Override
    public List<GlobalHipChatConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return commonConfigHandler.getConfig(id);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final GlobalHipChatConfigRestModel hipChatConfig) {
        return commonConfigHandler.postConfig(hipChatConfig);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final GlobalHipChatConfigRestModel hipChatConfig) {
        return commonConfigHandler.putConfig(hipChatConfig);
    }

    @Override
    public ResponseEntity<String> validateConfig(final GlobalHipChatConfigRestModel hipChatConfig) {
        return commonConfigHandler.validateConfig(hipChatConfig);
    }

    @Override
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final GlobalHipChatConfigRestModel hipChatConfig) {
        return commonConfigHandler.deleteConfig(hipChatConfig);
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final GlobalHipChatConfigRestModel hipChatConfig) {
        return commonConfigHandler.testConfig(hipChatConfig);
    }

}

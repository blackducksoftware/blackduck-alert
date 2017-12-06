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
package com.blackducksoftware.integration.hub.alert.web.controller.global;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalSlackConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.global.GlobalSlackConfigActions;
import com.blackducksoftware.integration.hub.alert.web.controller.CommonConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalSlackConfigRestModel;

@RestController
public class GlobalSlackConfigController extends ConfigController<GlobalSlackConfigRestModel> {
    private final CommonConfigController<GlobalSlackConfigEntity, GlobalSlackConfigRestModel> commonConfigController;

    @Autowired
    public GlobalSlackConfigController(final GlobalSlackConfigActions configActions) {
        commonConfigController = new CommonConfigController<>(GlobalSlackConfigEntity.class, GlobalSlackConfigRestModel.class, configActions);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/configuration/global/slack")
    public List<GlobalSlackConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return commonConfigController.getConfig(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/global/slack")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "slackConfig", required = true) @RequestBody final GlobalSlackConfigRestModel slackConfig) {
        return commonConfigController.postConfig(slackConfig);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/configuration/global/slack")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "slackConfig", required = true) @RequestBody final GlobalSlackConfigRestModel slackConfig) {
        return commonConfigController.putConfig(slackConfig);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/configuration/global/slack")
    public ResponseEntity<String> deleteConfig(@RequestAttribute(value = "slackConfig", required = true) @RequestBody final GlobalSlackConfigRestModel slackConfig) {
        return commonConfigController.deleteConfig(slackConfig);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/global/slack/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final GlobalSlackConfigRestModel slackConfig) {
        return commonConfigController.testConfig(slackConfig);
    }

    @Override
    public ResponseEntity<String> validateConfig(final GlobalSlackConfigRestModel restModel) {
        return commonConfigController.validateConfig(restModel);
    }

}

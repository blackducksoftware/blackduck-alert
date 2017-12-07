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
package com.blackducksoftware.integration.hub.alert.web.controller.distribution;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.distribution.HipChatDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.controller.CommonConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.HipChatDistributionRestModel;

@RestController
public class HipChatDistributionConfigController extends ConfigController<HipChatDistributionRestModel> {
    private final CommonConfigController<HipChatDistributionConfigEntity, HipChatDistributionRestModel> commonConfigController;

    @Autowired
    public HipChatDistributionConfigController(final HipChatDistributionConfigActions hipChatDistributionConfigActions) {
        commonConfigController = new CommonConfigController<>(HipChatDistributionConfigEntity.class, HipChatDistributionRestModel.class, hipChatDistributionConfigActions);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/configuration/distribution/hipchat")
    public List<HipChatDistributionRestModel> getConfig(final Long id) {
        return commonConfigController.getConfig(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/hipchat")
    public ResponseEntity<String> postConfig(final HipChatDistributionRestModel restModel) {
        return commonConfigController.postConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/configuration/distribution/hipchat")
    public ResponseEntity<String> putConfig(final HipChatDistributionRestModel restModel) {
        return commonConfigController.putConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/configuration/distribution/hipchat")
    public ResponseEntity<String> deleteConfig(final HipChatDistributionRestModel restModel) {
        // TODO improve and abstract for reuse
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/slack/hipchat")
    public ResponseEntity<String> testConfig(final HipChatDistributionRestModel restModel) {
        return commonConfigController.postConfig(restModel);
    }

    @Override
    public ResponseEntity<String> validateConfig(final HipChatDistributionRestModel restModel) {
        return commonConfigController.validateConfig(restModel);
    }

}

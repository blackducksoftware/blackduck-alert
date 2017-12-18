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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.distribution.SlackDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.controller.CommonConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;

@RestController
public class SlackDistributionConfigController extends ConfigController<SlackDistributionRestModel> {
    private final CommonConfigController<SlackDistributionConfigEntity, SlackDistributionRestModel> commonConfigController;

    @Autowired
    public SlackDistributionConfigController(final SlackDistributionConfigActions slackDistributionConfigActions) {
        commonConfigController = new CommonConfigController<>(SlackDistributionConfigEntity.class, SlackDistributionRestModel.class, slackDistributionConfigActions);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/configuration/distribution/slack")
    public List<SlackDistributionRestModel> getConfig(final Long id) {
        return commonConfigController.getConfig(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/slack")
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigController.postConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/configuration/distribution/slack")
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigController.putConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/configuration/distribution/slack")
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        // TODO improve and abstract for reuse
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/slack/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigController.testConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/slack/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigController.validateConfig(restModel);
    }

}

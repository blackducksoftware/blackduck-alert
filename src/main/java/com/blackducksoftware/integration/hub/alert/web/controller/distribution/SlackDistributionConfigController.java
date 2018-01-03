/**
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.distribution.SlackDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.CommonConfigHandler;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;

@RestController
public class SlackDistributionConfigController extends ConfigController<SlackDistributionRestModel> {
    private final CommonConfigHandler<SlackDistributionConfigEntity, SlackDistributionRestModel> commonConfigHandler;

    @Autowired
    public SlackDistributionConfigController(final SlackDistributionConfigActions slackDistributionConfigActions, final ObjectTransformer objectTransformer) {
        commonConfigHandler = new CommonConfigHandler<>(SlackDistributionConfigEntity.class, SlackDistributionRestModel.class, slackDistributionConfigActions, objectTransformer);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/configuration/distribution/slack")
    public List<SlackDistributionRestModel> getConfig(final Long id) {
        return commonConfigHandler.getConfig(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/slack")
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.postConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/configuration/distribution/slack")
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.putConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/configuration/distribution/slack")
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.doNotAllowHttpMethod();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/slack/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.testConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/slack/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.validateConfig(restModel);
    }

}

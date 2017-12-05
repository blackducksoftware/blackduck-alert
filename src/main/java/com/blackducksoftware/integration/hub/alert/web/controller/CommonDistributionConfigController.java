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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.web.actions.CommonDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.model.CommonDistributionConfigRestModel;

@RestController
public class CommonDistributionConfigController extends ConfigController<CommonDistributionConfigRestModel> {
    private final CommonConfigController<CommonDistributionConfigEntity, CommonDistributionConfigRestModel> commonConfigController;

    @Autowired
    public CommonDistributionConfigController(final CommonDistributionConfigActions commonDistributionConfigActions) {
        commonConfigController = new CommonConfigController<>(CommonDistributionConfigEntity.class, CommonDistributionConfigRestModel.class, commonDistributionConfigActions);
    }

    @Override
    @GetMapping(value = "/configuration/distribution/common")
    public List<CommonDistributionConfigRestModel> getConfig(final Long id) {
        return commonConfigController.getConfig(id);
    }

    @Override
    @PostMapping(value = "/configuration/distribution/common")
    public ResponseEntity<String> postConfig(@RequestBody(required = true) final CommonDistributionConfigRestModel restModel) {
        // TODO improve and abstract for reuse
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
    }

    @Override
    @PutMapping(value = "/configuration/distribution/common")
    public ResponseEntity<String> putConfig(@RequestBody(required = true) final CommonDistributionConfigRestModel restModel) {
        return commonConfigController.putConfig(restModel);
    }

    @Override
    @PostMapping(value = "/configuration/distribution/common/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) final CommonDistributionConfigRestModel restModel) {
        return commonConfigController.validateConfig(restModel);
    }

    @Override
    @DeleteMapping(value = "/configuration/distribution/common")
    public ResponseEntity<String> deleteConfig(@RequestBody(required = true) final CommonDistributionConfigRestModel restModel) {
        return commonConfigController.deleteConfig(restModel);
    }

    @Override
    @PostMapping(value = "/configuration/distribution/common/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = true) final CommonDistributionConfigRestModel restModel) {
        return commonConfigController.testConfig(restModel);
    }

}

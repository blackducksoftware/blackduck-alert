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
package com.blackducksoftware.integration.alert.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.web.actions.CommonDistributionConfigActions;
import com.blackducksoftware.integration.alert.web.controller.handler.CommonConfigHandler;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfig;

// TODO convert this over to the universal controller. Functionality SHOULD already be in.
@RestController
@RequestMapping(DistributionConfigController.DISTRIBUTION_PATH + "/common")
public class CommonDistributionConfigController extends DistributionConfigController<CommonDistributionConfig> {
    private final CommonConfigHandler<CommonDistributionConfigEntity, CommonDistributionConfig, CommonDistributionRepository> commonConfigHandler;

    @Autowired
    public CommonDistributionConfigController(final CommonDistributionConfigActions commonDistributionConfigActions, final ContentConverter contentConverter) {
        commonConfigHandler = new CommonConfigHandler<>(CommonDistributionConfigEntity.class, CommonDistributionConfig.class, commonDistributionConfigActions, contentConverter);
    }

    @Override
    public List<CommonDistributionConfig> getConfig(final Long id) {
        return commonConfigHandler.getConfig(id);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = true) final CommonDistributionConfig restModel) {
        return commonConfigHandler.doNotAllowHttpMethod();
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = true) final CommonDistributionConfig restModel) {
        return commonConfigHandler.putConfig(restModel);
    }

    @Override
    public ResponseEntity<String> validateConfig(@RequestBody(required = true) final CommonDistributionConfig restModel) {
        return commonConfigHandler.validateConfig(restModel);
    }

    @Override
    public ResponseEntity<String> deleteConfig(@RequestBody(required = true) final CommonDistributionConfig restModel) {
        return commonConfigHandler.deleteConfig(restModel);
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = true) final CommonDistributionConfig restModel) {
        return commonConfigHandler.testConfig(restModel);
    }

}

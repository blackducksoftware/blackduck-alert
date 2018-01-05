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

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.EmailGroupDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.distribution.EmailGroupDistributionConfigActions;
import com.blackducksoftware.integration.hub.alert.web.controller.ConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.CommonConfigHandler;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.EmailGroupDistributionRestModel;

@RestController
public class EmailGroupDistributionController extends ConfigController<EmailGroupDistributionRestModel> {
    private final CommonConfigHandler<EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel, EmailGroupDistributionRepositoryWrapper> commonConfigHandler;

    @Autowired
    public EmailGroupDistributionController(final EmailGroupDistributionConfigActions emailGroupDistributionConfigActions, final ObjectTransformer objectTransformer) {
        commonConfigHandler = new CommonConfigHandler<>(EmailGroupDistributionConfigEntity.class, EmailGroupDistributionRestModel.class, emailGroupDistributionConfigActions, objectTransformer);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/configuration/distribution/emailGroup")
    public List<EmailGroupDistributionRestModel> getConfig(final Long id) {
        return commonConfigHandler.getConfig(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/emailGroup")
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.postConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/configuration/distribution/emailGroup")
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.putConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/emailGroup/validate")
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.validateConfig(restModel);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(value = "/configuration/distribution/emailGroup")
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.doNotAllowHttpMethod();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/configuration/distribution/emailGroup/test")
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.testConfig(restModel);
    }

}

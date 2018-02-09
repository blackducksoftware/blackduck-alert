/**
 * hub-alert
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
package com.blackducksoftware.integration.hub.alert.channel.email.controller.distribution;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.distribution.EmailGroupDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.DistributionConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.CommonConfigHandler;

@RestController
@RequestMapping(DistributionConfigController.DISTRIBUTION_PATH + "/emailGroup")
public class EmailGroupDistributionConfigController extends DistributionConfigController<EmailGroupDistributionRestModel> {
    private final CommonConfigHandler<EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel, EmailGroupDistributionRepositoryWrapper> commonConfigHandler;

    @Autowired
    public EmailGroupDistributionConfigController(final EmailGroupDistributionConfigActions emailGroupDistributionConfigActions, final ObjectTransformer objectTransformer) {
        commonConfigHandler = new CommonConfigHandler<>(EmailGroupDistributionConfigEntity.class, EmailGroupDistributionRestModel.class, emailGroupDistributionConfigActions, objectTransformer);
    }

    @Override
    public List<EmailGroupDistributionRestModel> getConfig(final Long id) {
        return commonConfigHandler.getConfig(id);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.postConfig(restModel);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.putConfig(restModel);
    }

    @Override
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.validateConfig(restModel);
    }

    @Override
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.doNotAllowHttpMethod();
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final EmailGroupDistributionRestModel restModel) {
        return commonConfigHandler.testConfig(restModel);
    }

}

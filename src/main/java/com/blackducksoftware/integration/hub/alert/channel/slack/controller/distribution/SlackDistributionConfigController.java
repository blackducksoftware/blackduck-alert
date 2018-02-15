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
package com.blackducksoftware.integration.hub.alert.channel.slack.controller.distribution;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.slack.repository.distribution.SlackDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.DistributionConfigController;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.CommonConfigHandler;

@RestController
@RequestMapping(DistributionConfigController.DISTRIBUTION_PATH + "/slack")
public class SlackDistributionConfigController extends DistributionConfigController<SlackDistributionRestModel> {
    private final CommonConfigHandler<SlackDistributionConfigEntity, SlackDistributionRestModel, SlackDistributionRepositoryWrapper> commonConfigHandler;

    @Autowired
    public SlackDistributionConfigController(final SlackDistributionConfigActions slackDistributionConfigActions, final ObjectTransformer objectTransformer) {
        commonConfigHandler = new CommonConfigHandler<>(SlackDistributionConfigEntity.class, SlackDistributionRestModel.class, slackDistributionConfigActions, objectTransformer);
    }

    @Override
    public List<SlackDistributionRestModel> getConfig(final Long id) {
        return commonConfigHandler.getConfig(id);
    }

    @Override
    public ResponseEntity<String> postConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.postConfig(restModel);
    }

    @Override
    public ResponseEntity<String> putConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.putConfig(restModel);
    }

    @Override
    public ResponseEntity<String> deleteConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.doNotAllowHttpMethod();
    }

    @Override
    public ResponseEntity<String> testConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.testConfig(restModel);
    }

    @Override
    public ResponseEntity<String> validateConfig(@RequestBody(required = false) final SlackDistributionRestModel restModel) {
        return commonConfigHandler.validateConfig(restModel);
    }

}

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
package com.blackducksoftware.integration.hub.alert.web.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.manager.DistributionChannelManager;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.ControllerHandler;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

@Component
public class UniversalGlobalConfigActions extends ControllerHandler {
    private final DistributionChannelManager distributionChannelManager;
    private final UniversalConfigActions<ConfigRestModel> configActions;

    @Autowired
    public UniversalGlobalConfigActions(final ObjectTransformer objectTransformer, final DistributionChannelManager distributionChannelManager, final UniversalConfigActions<ConfigRestModel> configActions) {
        super(objectTransformer);
        this.distributionChannelManager = distributionChannelManager;
        this.configActions = configActions;
    }

    public String testConfig(final ConfigRestModel restModel, final String channelName) throws IntegrationException {
        return distributionChannelManager.testGlobalConfig(channelName, restModel);
    }

    public UniversalConfigActions<ConfigRestModel> getConfigActions() {
        return configActions;
    }

}

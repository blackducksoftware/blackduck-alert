/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.provider.blackduck.factory;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderPropertiesFactory;
import com.synopsys.integration.alert.common.rest.ProxyManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;

@Component
public class BlackDuckPropertiesFactory extends ProviderPropertiesFactory<BlackDuckProperties> {
    private final Gson gson;
    private final AlertProperties alertProperties;
    private final ProxyManager proxyManager;
    private final ConfigurationAccessor configurationAccessor;

    @Autowired
    public BlackDuckPropertiesFactory(Gson gson, AlertProperties alertProperties, ProxyManager proxyManager, ConfigurationAccessor configurationAccessor) {
        this.gson = gson;
        this.alertProperties = alertProperties;
        this.proxyManager = proxyManager;
        this.configurationAccessor = configurationAccessor;
    }

    @Override
    public BlackDuckProperties createProperties(Long blackDuckConfigId, FieldUtility fieldUtility) {
        return new BlackDuckProperties(blackDuckConfigId, gson, alertProperties, proxyManager, fieldUtility);
    }

    public Optional<BlackDuckProperties> createPropertiesIfConfigExists(Long blackDuckConfigId) {
        Optional<ConfigurationModel> optionalBlackDuckConfig = configurationAccessor.getConfigurationById(blackDuckConfigId);
        if (optionalBlackDuckConfig.isPresent()) {
            BlackDuckProperties blackDuckProperties = createProperties(optionalBlackDuckConfig.get());
            return Optional.of(blackDuckProperties);
        }
        return Optional.empty();
    }

}

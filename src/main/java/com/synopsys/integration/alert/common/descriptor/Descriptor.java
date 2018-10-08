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
package com.synopsys.integration.alert.common.descriptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.descriptor.config.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ActionApiType;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public abstract class Descriptor {
    private final String name;
    private final DescriptorType type;
    private final Map<ActionApiType, RestApi> restApis;
    private final Map<ActionApiType, UIConfig> uiConfigs;

    public Descriptor(final String name, final DescriptorType type) {
        this.name = name;
        this.type = type;
        restApis = new HashMap<>();
        uiConfigs = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

    public void addProviderRestApi(final RestApi restApi) {
        restApis.put(ActionApiType.PROVIDER_CONFIG, restApi);
    }

    public void addGlobalRestApi(final RestApi restApi) {
        restApis.put(ActionApiType.CHANNEL_GLOBAL_CONFIG, restApi);
    }

    public void addChannelDistributionRestApi(final RestApi restApi) {
        restApis.put(ActionApiType.CHANNEL_DISTRIBUTION_CONFIG, restApi);
    }

    public void addProviderDistributionRestApi(final RestApi restApi) {
        restApis.put(ActionApiType.PROVIDER_DISTRIBUTION_CONFIG, restApi);
    }

    public void addComponentRestApi(final RestApi restApi) {
        restApis.put(ActionApiType.COMPONENT_CONFIG, restApi);
    }

    public void addProviderUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(ActionApiType.PROVIDER_CONFIG, uiConfig);
        addProviderRestApi(restApi);
    }

    public void addGlobalUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(ActionApiType.CHANNEL_GLOBAL_CONFIG, uiConfig);
        addGlobalRestApi(restApi);
    }

    public void addChannelDistributionUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(ActionApiType.CHANNEL_DISTRIBUTION_CONFIG, uiConfig);
        addChannelDistributionRestApi(restApi);
    }

    public void addProviderDistributionUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(ActionApiType.PROVIDER_DISTRIBUTION_CONFIG, uiConfig);
        addProviderDistributionRestApi(restApi);
    }

    public void addComponentUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(ActionApiType.COMPONENT_CONFIG, uiConfig);
        addComponentRestApi(restApi);
    }

    public RestApi getRestApi(final ActionApiType actionApiType) {
        return restApis.get(actionApiType);
    }

    public UIConfig getUIConfig(final ActionApiType actionApiType) {
        return uiConfigs.get(actionApiType);
    }

    public List<UIConfig> getAllUIConfigs() {
        if (hasUIConfigs()) {
            return uiConfigs.values().stream().collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public boolean hasUIConfigs() {
        return uiConfigs.size() > 0;
    }

    public boolean hasUIConfigForType(final ActionApiType actionApiType) {
        return uiConfigs.containsKey(actionApiType);
    }

    public Optional<? extends DatabaseEntity> readEntity(final ActionApiType actionApiType, final long id) {
        return getRestApi(actionApiType).readEntity(id);
    }

    public List<? extends DatabaseEntity> readEntities(final ActionApiType actionApiType) {
        return getRestApi(actionApiType).readEntities();
    }

    public DatabaseEntity saveEntity(final ActionApiType actionApiType, final DatabaseEntity entity) {
        return getRestApi(actionApiType).saveEntity(entity);
    }

    public void deleteEntity(final ActionApiType actionApiType, final long id) {
        getRestApi(actionApiType).deleteEntity(id);
    }

    public DatabaseEntity populateEntityFromConfig(final ActionApiType actionApiType, final Config config) {
        return getRestApi(actionApiType).populateEntityFromConfig(config);
    }

    public Config populateConfigFromEntity(final ActionApiType actionApiType, final DatabaseEntity entity) {
        return getRestApi(actionApiType).populateConfigFromEntity(entity);
    }

    public Config getConfigFromJson(final ActionApiType actionApiType, final String json) {
        return getRestApi(actionApiType).getConfigFromJson(json);
    }

    public void validateConfig(final ActionApiType actionApiType, final Config config, final Map<String, String> fieldErrors) {
        getRestApi(actionApiType).validateConfig(config, fieldErrors);
    }

    public void testConfig(final ActionApiType actionApiType, final Config config) throws IntegrationException {
        getRestApi(actionApiType).testConfig(config);
    }

}

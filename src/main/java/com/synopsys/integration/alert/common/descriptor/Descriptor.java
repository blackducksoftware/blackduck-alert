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
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.DescriptorActionApi;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public abstract class Descriptor {
    private final String name;
    private final DescriptorType type;
    private final Map<DescriptorActionApi, RestApi> restApis;
    private final Map<DescriptorActionApi, UIConfig> uiConfigs;

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
        restApis.put(DescriptorActionApi.PROVIDER_CONFIG, restApi);
    }

    public void addGlobalRestApi(final RestApi restApi) {
        restApis.put(DescriptorActionApi.CHANNEL_GLOBAL_CONFIG, restApi);
    }

    public void addChannelDistributionRestApi(final RestApi restApi) {
        restApis.put(DescriptorActionApi.CHANNEL_DISTRIBUTION_CONFIG, restApi);
    }

    public void addProviderDistributionRestApi(final RestApi restApi) {
        restApis.put(DescriptorActionApi.PROVIDER_DISTRIBUTION_CONFIG, restApi);
    }

    public void addComponentRestApi(final RestApi restApi) {
        restApis.put(DescriptorActionApi.COMPONENT_CONFIG, restApi);
    }

    public void addProviderUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(DescriptorActionApi.PROVIDER_CONFIG, uiConfig);
        addProviderRestApi(restApi);
    }

    public void addGlobalUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(DescriptorActionApi.CHANNEL_GLOBAL_CONFIG, uiConfig);
        addGlobalRestApi(restApi);
    }

    public void addChannelDistributionUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(DescriptorActionApi.CHANNEL_DISTRIBUTION_CONFIG, uiConfig);
        addChannelDistributionRestApi(restApi);
    }

    public void addProviderDistributionUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(DescriptorActionApi.PROVIDER_DISTRIBUTION_CONFIG, uiConfig);
        addProviderDistributionRestApi(restApi);
    }

    public void addComponentUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(DescriptorActionApi.COMPONENT_CONFIG, uiConfig);
        addComponentRestApi(restApi);
    }

    public RestApi getRestApi(final DescriptorActionApi descriptorActionApi) {
        return restApis.get(descriptorActionApi);
    }

    public UIConfig getUIConfig(final DescriptorActionApi descriptorActionApi) {
        return uiConfigs.get(descriptorActionApi);
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

    public boolean hasUIConfigForType(final DescriptorActionApi descriptorActionApi) {
        return uiConfigs.containsKey(descriptorActionApi);
    }

    public Optional<? extends DatabaseEntity> readEntity(final DescriptorActionApi descriptorActionApi, final long id) {
        return getRestApi(descriptorActionApi).readEntity(id);
    }

    public List<? extends DatabaseEntity> readEntities(final DescriptorActionApi descriptorActionApi) {
        return getRestApi(descriptorActionApi).readEntities();
    }

    public DatabaseEntity saveEntity(final DescriptorActionApi descriptorActionApi, final DatabaseEntity entity) {
        return getRestApi(descriptorActionApi).saveEntity(entity);
    }

    public void deleteEntity(final DescriptorActionApi descriptorActionApi, final long id) {
        getRestApi(descriptorActionApi).deleteEntity(id);
    }

    public DatabaseEntity populateEntityFromConfig(final DescriptorActionApi descriptorActionApi, final Config config) {
        return getRestApi(descriptorActionApi).populateEntityFromConfig(config);
    }

    public Config populateConfigFromEntity(final DescriptorActionApi descriptorActionApi, final DatabaseEntity entity) {
        return getRestApi(descriptorActionApi).populateConfigFromEntity(entity);
    }

    public Config getConfigFromJson(final DescriptorActionApi descriptorActionApi, final String json) {
        return getRestApi(descriptorActionApi).getConfigFromJson(json);
    }

    public void validateConfig(final DescriptorActionApi descriptorActionApi, final Config config, final Map<String, String> fieldErrors) {
        getRestApi(descriptorActionApi).validateConfig(config, fieldErrors);
    }

    public void testConfig(final DescriptorActionApi descriptorActionApi, final Config config) throws IntegrationException {
        getRestApi(descriptorActionApi).testConfig(config);
    }

}

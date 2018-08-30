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
import com.synopsys.integration.alert.common.enumeration.RestApiType;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public abstract class Descriptor {
    private final String name;
    private final DescriptorType type;
    private final Map<RestApiType, RestApi> restApis;
    private final Map<RestApiType, UIConfig> uiConfigs;

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
        restApis.put(RestApiType.PROVIDER_CONFIG, restApi);
    }

    public void addGlobalRestApi(final RestApi restApi) {
        restApis.put(RestApiType.CHANNEL_GLOBAL_CONFIG, restApi);
    }

    public void addDistributionRestApi(final RestApi restApi) {
        restApis.put(RestApiType.CHANNEL_DISTRIBUTION_CONFIG, restApi);
    }

    public void addComponentRestApi(final RestApi restApi) {
        restApis.put(RestApiType.COMPONENT_CONFIG, restApi);
    }

    public void addProviderUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(RestApiType.PROVIDER_CONFIG, uiConfig);
        addProviderRestApi(restApi);
    }

    public void addGlobalUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(RestApiType.CHANNEL_GLOBAL_CONFIG, uiConfig);
        addGlobalRestApi(restApi);
    }

    public void addDistributionUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(RestApiType.CHANNEL_DISTRIBUTION_CONFIG, uiConfig);
        addDistributionRestApi(restApi);
    }

    public void addComponentUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(RestApiType.COMPONENT_CONFIG, uiConfig);
        addComponentRestApi(restApi);
    }

    public RestApi getRestApi(final RestApiType restApiType) {
        return restApis.get(restApiType);
    }

    public UIConfig getUIConfig(final RestApiType restApiType) {
        return uiConfigs.get(restApiType);
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

    public boolean hasUIConfigForType(final RestApiType restApiType) {
        return uiConfigs.containsKey(restApiType);
    }

    public Optional<? extends DatabaseEntity> readEntity(final RestApiType restApiType, final long id) {
        return getRestApi(restApiType).readEntity(id);
    }

    public List<? extends DatabaseEntity> readEntities(final RestApiType restApiType) {
        return getRestApi(restApiType).readEntities();
    }

    public DatabaseEntity saveEntity(final RestApiType restApiType, final DatabaseEntity entity) {
        return getRestApi(restApiType).saveEntity(entity);
    }

    public void deleteEntity(final RestApiType restApiType, final long id) {
        getRestApi(restApiType).deleteEntity(id);
    }

    public DatabaseEntity populateEntityFromConfig(final RestApiType restApiType, final Config config) {
        return getRestApi(restApiType).populateEntityFromConfig(config);
    }

    public Config populateConfigFromEntity(final RestApiType restApiType, final DatabaseEntity entity) {
        return getRestApi(restApiType).populateConfigFromEntity(entity);
    }

    public Config getConfigFromJson(final RestApiType restApiType, final String json) {
        return getRestApi(restApiType).getConfigFromJson(json);
    }

    public void validateConfig(final RestApiType restApiType, final Config config, final Map<String, String> fieldErrors) {
        getRestApi(restApiType).validateConfig(config, fieldErrors);
    }

    public void testConfig(final RestApiType restApiType, final DatabaseEntity entity) throws IntegrationException {
        getRestApi(restApiType).testConfig(entity);
    }

}

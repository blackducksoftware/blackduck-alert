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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.common.descriptor.config.RestApi;
import com.synopsys.integration.alert.common.descriptor.config.UIConfig;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.RestApiTypes;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public abstract class Descriptor {
    private final String name;
    private final DescriptorType type;
    private final Map<RestApiTypes, RestApi> restApis;
    private final Map<RestApiTypes, UIConfig> uiConfigs;

    public Descriptor(final String name, final DescriptorType type) {
        this.name = name;
        this.type = type;
        restApis = new HashMap<>(RestApiTypes.values().length + 1);
        uiConfigs = new HashMap<>(RestApiTypes.values().length + 1);
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

    public void addProviderRestApi(final RestApi restApi) {
        restApis.put(RestApiTypes.PROVIDER_CONFIG, restApi);
    }

    public void addGlobalRestApi(final RestApi restApi) {
        restApis.put(RestApiTypes.CHANNEL_GLOBAL_CONFIG, restApi);
    }

    public void addDistributionRestApi(final RestApi restApi) {
        restApis.put(RestApiTypes.CHANNEL_DISTRIBUTION_CONFIG, restApi);
    }

    public void addComponentRestApi(final RestApi restApi) {
        restApis.put(RestApiTypes.COMPONENT_CONFIG, restApi);
    }

    public void addProviderUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(RestApiTypes.PROVIDER_CONFIG, uiConfig);
        addProviderRestApi(restApi);
    }

    public void addGlobalUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(RestApiTypes.CHANNEL_GLOBAL_CONFIG, uiConfig);
        addGlobalRestApi(restApi);
    }

    public void addDistributionUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(RestApiTypes.CHANNEL_DISTRIBUTION_CONFIG, uiConfig);
        addDistributionRestApi(restApi);
    }

    public void addComponentUiConfigs(final RestApi restApi, final UIConfig uiConfig) {
        uiConfigs.put(RestApiTypes.COMPONENT_CONFIG, uiConfig);
        addComponentRestApi(restApi);
    }

    public RestApi getRestApi(final RestApiTypes restApiTypes) {
        return restApis.get(restApiTypes);
    }

    public UIConfig getUIConfig(final RestApiTypes restApiTypes) {
        return uiConfigs.get(restApiTypes);
    }

    public Optional<? extends DatabaseEntity> readEntity(final RestApiTypes restApiTypes, final long id) {
        return getRestApi(restApiTypes).readEntity(id);
    }

    public List<? extends DatabaseEntity> readEntities(final RestApiTypes restApiTypes) {
        return getRestApi(restApiTypes).readEntities();
    }

    public DatabaseEntity saveEntity(final RestApiTypes restApiTypes, final DatabaseEntity entity) {
        return getRestApi(restApiTypes).saveEntity(entity);
    }

    public void deleteEntity(final RestApiTypes restApiTypes, final long id) {
        getRestApi(restApiTypes).deleteEntity(id);
    }

    public DatabaseEntity populateEntityFromConfig(final RestApiTypes restApiTypes, final Config config) {
        return getRestApi(restApiTypes).populateEntityFromConfig(config);
    }

    public Config populateConfigFromEntity(final RestApiTypes restApiTypes, final DatabaseEntity entity) {
        return getRestApi(restApiTypes).populateConfigFromEntity(entity);
    }

    public Config getConfigFromJson(final RestApiTypes restApiTypes, final String json) {
        return getRestApi(restApiTypes).getConfigFromJson(json);
    }

    public void validateConfig(final RestApiTypes restApiTypes, final Config config, final Map<String, String> fieldErrors) {
        getRestApi(restApiTypes).validateConfig(config, fieldErrors);
    }

    public void testConfig(final RestApiTypes restApiTypes, final DatabaseEntity entity) throws IntegrationException {
        getRestApi(restApiTypes).testConfig(entity);
    }

}

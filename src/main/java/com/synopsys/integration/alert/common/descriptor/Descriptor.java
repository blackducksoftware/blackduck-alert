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
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.DescriptorConfig;
import com.synopsys.integration.alert.common.enumeration.DescriptorConfigType;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;

public abstract class Descriptor {
    private final String name;
    private final DescriptorType type;
    private final Map<DescriptorConfigType, DescriptorConfig> descriptorConfigs;

    public Descriptor(final String name, final DescriptorType type) {
        this.name = name;
        this.type = type;
        descriptorConfigs = new HashMap<>(3);
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

    public void addProviderConfig(final DescriptorConfig descriptorConfig) {
        descriptorConfigs.put(DescriptorConfigType.PROVIDER_CONFIG, descriptorConfig);
    }

    public void addGlobalConfig(final DescriptorConfig descriptorConfig) {
        descriptorConfigs.put(DescriptorConfigType.CHANNEL_GLOBAL_CONFIG, descriptorConfig);
    }

    public void addDistributionConfig(final DescriptorConfig descriptorConfig) {
        descriptorConfigs.put(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG, descriptorConfig);
    }

    public void addComponentConfig(final DescriptorConfig descriptorConfig) {
        descriptorConfigs.put(DescriptorConfigType.COMPONENT_CONFIG, descriptorConfig);
    }

    public DescriptorConfig getConfig(final DescriptorConfigType descriptorConfigType) {
        return descriptorConfigs.get(descriptorConfigType);
    }

    public Set<DescriptorConfig> getAllConfigs() {
        return descriptorConfigs.values().stream().collect(Collectors.toSet());
    }

    public Optional<? extends DatabaseEntity> readEntity(final DescriptorConfigType descriptorConfigType, final long id) {
        return getConfig(descriptorConfigType).readEntity(id);
    }

    public List<? extends DatabaseEntity> readEntities(final DescriptorConfigType descriptorConfigType) {
        return getConfig(descriptorConfigType).readEntities();
    }

    public DatabaseEntity saveEntity(final DescriptorConfigType descriptorConfigType, final DatabaseEntity entity) {
        return getConfig(descriptorConfigType).saveEntity(entity);
    }

    public void deleteEntity(final DescriptorConfigType descriptorConfigType, final long id) {
        getConfig(descriptorConfigType).deleteEntity(id);
    }

    public DatabaseEntity populateEntityFromConfig(final DescriptorConfigType descriptorConfigType, final Config config) {
        return getConfig(descriptorConfigType).populateEntityFromConfig(config);
    }

    public Config populateConfigFromEntity(final DescriptorConfigType descriptorConfigType, final DatabaseEntity entity) {
        return getConfig(descriptorConfigType).populateConfigFromEntity(entity);
    }

    public Config getConfigFromJson(final DescriptorConfigType descriptorConfigType, final String json) {
        return getConfig(descriptorConfigType).getConfigFromJson(json);
    }

    public void validateConfig(final DescriptorConfigType descriptorConfigType, final Config config, final Map<String, String> fieldErrors) {
        getConfig(descriptorConfigType).validateConfig(config, fieldErrors);
    }

    public void testConfig(final DescriptorConfigType descriptorConfigType, final DatabaseEntity entity) throws IntegrationException {
        getConfig(descriptorConfigType).testConfig(entity);
    }

}

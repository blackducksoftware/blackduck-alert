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
package com.blackducksoftware.integration.alert.common.descriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfigType;

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
        descriptorConfigs.put(DescriptorConfigType.GLOBAL_CONFIG, descriptorConfig);
    }

    public void addDistributionConfig(final DescriptorConfig descriptorConfig) {
        descriptorConfigs.put(DescriptorConfigType.DISTRIBUTION_CONFIG, descriptorConfig);
    }

    public DescriptorConfig getConfig(final DescriptorConfigType descriptorConfigType) {
        return descriptorConfigs.get(descriptorConfigType);
    }

    public Set<DescriptorConfig> getAllConfigs() {
        return descriptorConfigs.values().stream().collect(Collectors.toSet());
    }

}

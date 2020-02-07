/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public abstract class Descriptor extends AlertSerializableModel {
    private final String name;
    private final DescriptorType type;
    private final Map<ConfigContextEnum, UIConfig> uiConfigs;

    public Descriptor(final String name, final DescriptorType type) {
        this.name = name;
        this.type = type;
        uiConfigs = new EnumMap<>(ConfigContextEnum.class);
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

    public void addGlobalUiConfig(final UIConfig uiConfig) {
        uiConfigs.put(ConfigContextEnum.GLOBAL, uiConfig);
    }

    public void addDistributionUiConfig(final UIConfig uiConfig) {
        uiConfigs.put(ConfigContextEnum.DISTRIBUTION, uiConfig);
    }

    public Optional<UIConfig> getUIConfig(final ConfigContextEnum actionApiType) {
        return Optional.ofNullable(uiConfigs.get(actionApiType));
    }

    public Optional<DescriptorMetadata> createMetaData(final ConfigContextEnum context) {
        return getUIConfig(context).map(uiConfig -> createMetaData(uiConfig, context));
    }

    public Set<DefinedFieldModel> getAllDefinedFields(final ConfigContextEnum context) {
        return getUIConfig(context)
                   .map(UIConfig::createFields)
                   .orElse(List.of())
                   .stream()
                   .map(configField -> new DefinedFieldModel(configField.getKey(), context, configField.isSensitive()))
                   .collect(Collectors.toSet());
    }

    public Set<ConfigContextEnum> getAppliedUIContexts() {
        return uiConfigs.keySet();
    }

    public boolean hasUIConfigs() {
        return uiConfigs.size() > 0;
    }

    public boolean hasUIConfigForType(final ConfigContextEnum actionApiType) {
        return uiConfigs.containsKey(actionApiType);
    }

    private DescriptorMetadata createMetaData(final UIConfig uiConfig, final ConfigContextEnum context) {
        final String label = uiConfig.getLabel();
        final String urlName = uiConfig.getUrlName();
        final String fontAwesomeIcon = uiConfig.getFontAwesomeIcon();
        final String description = uiConfig.getDescription();
        final boolean autoGenerateUI = uiConfig.autoGenerateUI();
        final String componentNamespace = uiConfig.getComponentNamespace();
        return new DescriptorMetadata(label, urlName, getName(), description, getType(), context, fontAwesomeIcon, autoGenerateUI, componentNamespace, uiConfig.createFields(), uiConfig.createTestLabel());
    }

}

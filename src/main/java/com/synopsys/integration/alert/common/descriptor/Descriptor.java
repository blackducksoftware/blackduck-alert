/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.Stringable;

public abstract class Descriptor extends Stringable {
    private final String name;
    private final DescriptorType type;
    private final Map<ConfigContextEnum, DescriptorActionApi> actionApis;
    private final Map<ConfigContextEnum, UIConfig> uiConfigs;

    public Descriptor(final String name, final DescriptorType type) {
        this.name = name;
        this.type = type;
        actionApis = new EnumMap<>(ConfigContextEnum.class);
        uiConfigs = new EnumMap<>(ConfigContextEnum.class);
    }

    public String getName() {
        return name;
    }

    public DescriptorType getType() {
        return type;
    }

    public void addGlobalActionApi(final DescriptorActionApi descriptorActionApi) {
        actionApis.put(ConfigContextEnum.GLOBAL, descriptorActionApi);
    }

    public void addDistributionActionApi(final DescriptorActionApi descriptorActionApi) {
        actionApis.put(ConfigContextEnum.DISTRIBUTION, descriptorActionApi);
    }

    public void addGlobalUiConfig(final DescriptorActionApi descriptorActionApi, final UIConfig uiConfig) {
        uiConfigs.put(ConfigContextEnum.GLOBAL, uiConfig);
        addGlobalActionApi(descriptorActionApi);
    }

    public void addDistributionUiConfig(final DescriptorActionApi descriptorActionApi, final UIConfig uiConfig) {
        uiConfigs.put(ConfigContextEnum.DISTRIBUTION, uiConfig);
        addDistributionActionApi(descriptorActionApi);
    }

    public Optional<DescriptorActionApi> getActionApi(final ConfigContextEnum actionApiType) {
        return Optional.ofNullable(actionApis.get(actionApiType));
    }

    public Optional<UIConfig> getUIConfig(final ConfigContextEnum actionApiType) {
        return Optional.ofNullable(uiConfigs.get(actionApiType));
    }

    public Optional<DescriptorMetadata> getMetaData(final ConfigContextEnum context) {
        return getUIConfig(context).map(uiConfig -> createMetaData(uiConfig, context));
    }

    public Set<DefinedFieldModel> getAllDefinedFields(final ConfigContextEnum context) {
        return getUIConfig(context)
                   .map(uiConfig -> uiConfig.createFields())
                   .orElse(List.of())
                   .stream()
                   .map(configField -> new DefinedFieldModel(configField.getKey(), context, configField.isSensitive()))
                   .collect(Collectors.toSet());
    }

    public List<DescriptorMetadata> getAllMetaData() {
        if (hasUIConfigs()) {
            return uiConfigs.entrySet()
                       .stream()
                       .map(entry -> createMetaData(entry.getValue(), entry.getKey()))
                       .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
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

    public void validateConfig(final ConfigContextEnum configContext, final FieldModel fieldModel, final Map<String, String> fieldErrors) {
        final Optional<DescriptorActionApi> actionApi = getActionApi(configContext);
        final Optional<UIConfig> uiConfig = getUIConfig(configContext);
        final Map<String, ConfigField> configFieldMap = getUIConfig(configContext)
                                                            .map(config -> config.createFields())
                                                            .map(fieldList -> fieldList.stream()
                                                                                  .collect(Collectors.toMap(ConfigField::getKey, Function.identity())))
                                                            .orElse(Map.of());
        uiConfig.ifPresent(config ->
                               actionApi.ifPresent(descriptorActionApi -> descriptorActionApi.validateConfig(configFieldMap, fieldModel, fieldErrors)));
    }

    public void testConfig(final ConfigContextEnum actionApiType, final TestConfigModel testConfig) throws IntegrationException {
        final Optional<DescriptorActionApi> actionApi = getActionApi(actionApiType);
        if (actionApi.isPresent()) {
            actionApi.get().testConfig(testConfig);
        }
    }

    private DescriptorMetadata createMetaData(final UIConfig uiConfig, final ConfigContextEnum context) {
        final String label = uiConfig.getLabel();
        final String urlName = uiConfig.getUrlName();
        final String fontAwesomeIcon = uiConfig.getFontAwesomeIcon();
        return new DescriptorMetadata(label, urlName, getName(), getType(), context, fontAwesomeIcon, uiConfig.createFields());
    }

    private List<ConfigField> retrieveUIConfigFields(final ConfigContextEnum context) {
        final Optional<UIConfig> uiConfig = getUIConfig(context);
        return uiConfig.map(config -> config.createFields()).orElse(List.of());
    }

    private Map<String, ConfigField> createConfigFieldMap(final ConfigContextEnum context) {
        final List<ConfigField> configFields = retrieveUIConfigFields(context);
        return configFields.stream().collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
    }
}

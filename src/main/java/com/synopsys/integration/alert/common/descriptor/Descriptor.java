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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.database.api.configuration.DefinedFieldModel;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class Descriptor {
    private final String name;
    private final DescriptorType type;
    private final Map<ConfigContextEnum, DescriptorActionApi> restApis;
    private final Map<ConfigContextEnum, UIConfig> uiConfigs;

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

    public void addGlobalActionApi(final DescriptorActionApi descriptorActionApi) {
        restApis.put(ConfigContextEnum.GLOBAL, descriptorActionApi);
    }

    public void addDistributionActionApi(final DescriptorActionApi descriptorActionApi) {
        restApis.put(ConfigContextEnum.DISTRIBUTION, descriptorActionApi);
    }

    public void addGlobalUiConfig(final DescriptorActionApi descriptorActionApi, final UIConfig uiConfig) {
        uiConfigs.put(ConfigContextEnum.GLOBAL, uiConfig);
        addGlobalActionApi(descriptorActionApi);
    }

    public void addDistributionUiConfig(final DescriptorActionApi descriptorActionApi, final UIConfig uiConfig) {
        uiConfigs.put(ConfigContextEnum.DISTRIBUTION, uiConfig);
        addDistributionActionApi(descriptorActionApi);
    }

    public DescriptorActionApi getRestApi(final ConfigContextEnum actionApiType) {
        return restApis.get(actionApiType);
    }

    public UIConfig getUIConfig(final ConfigContextEnum actionApiType) {
        return uiConfigs.get(actionApiType);
    }

    public List<UIConfig> getAllUIConfigs() {
        if (hasUIConfigs()) {
            return uiConfigs.values().stream().collect(Collectors.toList());
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

    public void validateConfig(final ConfigContextEnum actionApiType, final FieldAccessor fieldAccessor, final Map<String, String> fieldErrors) {
        getRestApi(actionApiType).validateConfig(fieldAccessor, fieldErrors);
    }

    public void testConfig(final ConfigContextEnum actionApiType, final TestConfigModel testConfig) throws IntegrationException {
        getRestApi(actionApiType).testConfig(testConfig);
    }

    public Collection<DefinedFieldModel> createAllDefinedFields() {
        final Set<ConfigContextEnum> appliedUIContexts = getAppliedUIContexts();
        final List<DefinedFieldModel> fieldModels = new ArrayList<>();
        for (final ConfigContextEnum context : appliedUIContexts) {
            final UIConfig uiConfig = getUIConfig(context);
            final List<ConfigField> fields = uiConfig.generateUIComponent().getFields();
            for (final ConfigField field : fields) {
                final String key = field.getKey();
                final boolean isSensitive = field.isSensitive();
                fieldModels.add(new DefinedFieldModel(key, context, isSensitive));
            }
        }
        return fieldModels;
    }
}
